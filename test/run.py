import os
import subprocess as sp
import time
from typing import List

from Config import *

TIME_OUT = 45

my_jar: sp.Popen


def run_command(cmd: str, input_path: str = None, output_path: str = None):
    try:
        sp.Popen(
            cmd.split(),
            stdin=None if input_path is None else open(input_path, "r"),
            stdout=None if output_path is None else open(output_path, "w"),
        ).wait(TIME_OUT)
    except sp.TimeoutExpired:
        raise sp.TimeoutExpired("Time out when executing command: " + cmd, TIME_OUT)


def make_testfile(src_path: str):
    with open("testfile.txt", "w", encoding="utf-8") as testfile, open(src_path, "r", encoding="utf-8") as src:
        testfile.write(src.read())
        src.close()
        testfile.close()


def compare(path1: str, path2: str) -> bool:
    with open(path1, "r") as f1, open(path2, "r") as f2:
        content1 = f1.read().split()
        content2 = f2.read().split()
        if len(content1) != len(content2):
            print("Length not equal: " + str(len(content1)) + " " + str(len(content2)))
            print("Content1: " + str(content1))
            print("Content2: " + str(content2))
            return False
        for i in range(len(content1)):
            if content1[i] != content2[i]:
                print("Not equal: " + content1[i] + " " + content2[i])
                return False
        return True


def run_my_jar(src_path: str):
    my_jar.stdin.write(bytes(src_path + "\n", encoding="utf-8"))
    my_jar.stdin.flush()
    ret = my_jar.stdout.readline().decode("utf-8")
    if ret.split()[0] != "Done":
        print("Error when running my jar!")
        exit(1)


def run_llvm_and_compare() -> bool:
    try:
        run_command("llvm-link " + MY_IR + " " + STD_LIB_LLIR + " -S -o " + MY_LINKED_LLIR)
    except sp.TimeoutExpired:
        print("Time out When Linking my IR!")
    try:
        run_command("lli " + MY_LINKED_LLIR, input_path=TEMP_INPUT_PATH, output_path=MY_IR_RESULT)
    except sp.TimeoutExpired:
        print("Time out When Executing my IR!")
    return compare(STD_RESULT, MY_IR_RESULT)


def run_mips_and_compare() -> bool:
    try:
        run_command("java -jar " + MARS_PATH + " nc " + MY_MIPS, input_path=TEMP_INPUT_PATH, output_path=MY_MIPS_RESULT)
    except sp.TimeoutExpired:
        print("Time out When Executing my MIPS!")
    return compare(STD_RESULT, MY_MIPS_RESULT)


def test_with_file(src_path: str, input_path: str, ans_path: str):
    make_testfile(src_path)
    with open(TEMP_INPUT_PATH, "w") as input_file, open(input_path, "r") as input_src:
        for num in input_src.read().split():
            input_file.write(num + "\n")
    open(STD_RESULT, "w").write(open(ans_path, "r").read())
    run_my_jar(src_path)
    # passed = run_llvm_and_compare() and run_mips_and_compare()
    passed = run_mips_and_compare()
    print(time.strftime("[%H:%M:%S]") + ("Passed" if passed else "Failed") + " Test: " + src_path + "\n")
    if not passed:
        exit(1)
    return passed


def collect_test_in_dir(dir_path: str) -> List[str]:
    result = []
    for file in os.listdir(dir_path):
        if file.startswith("testfile") and file.endswith(".txt"):
            result.append(file)
    return result


if __name__ == "__main__":

    allPassed = True
    my_jar = sp.Popen(("java -jar " + JAR_PATH + " -server").split(), stdin=sp.PIPE, stdout=sp.PIPE)
    for theme in ["20A", "20B", "20C"]:
        theme_path = TESTCASE_PATH + theme + "/"
        for test in collect_test_in_dir(theme_path):
            allPassed &= test_with_file(
                theme_path + test,
                theme_path + test.replace("testfile", "input"),
                theme_path + test.replace("testfile", "output")
            )
    my_jar.stdin.close()
    my_jar.wait()

    exit(0 if allPassed else 1)
