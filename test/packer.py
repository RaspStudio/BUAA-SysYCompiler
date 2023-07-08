import os
import re

from Config import *


def get_paths(root_path: str) -> str:
    ret = ""
    for root, dirs, files in os.walk(root_path, topdown=False):
        for name in files:
            if re.search(r"\.java$", name) is not None:
                ret += os.path.join(root, name) + "\n"
    return ret


def java_to_class(src_path: str, out_path: str):
    if not os.path.exists(out_path):
        os.mkdir(out_path)

    tempListPath = "javaFileList.txt"
    tempList = open(tempListPath, "w")
    tempList.write(get_paths(src_path))
    tempList.close()

    os.system("javac -encoding UTF-8 -Xlint:unchecked" +
              " -d " + out_path + " -sourcepath " + src_path + " @" + tempListPath)
    os.remove(tempListPath)
    pass


def class_to_jar(class_path: str, jar_path: str, entry_name: str):
    temp_manifest_path = "mf.tmp"
    with open(temp_manifest_path, "w") as manifest:
        manifest.write("Main-Class: " + entry_name + "\n")
        manifest.close()
        if os.path.exists(jar_path):
            os.remove(jar_path)
        os.system("jar cvfm " + jar_path + " " + temp_manifest_path + " -C " + class_path + " .")
        os.remove(temp_manifest_path)
    pass


def pack_mine(src_path: str, out_path: str, jar_path: str, jar_entry: str):
    if not os.path.exists(out_path):
        os.mkdir(out_path)

    java_to_class(src_path, out_path)

    class_to_jar(out_path, jar_path, jar_entry)
    print("\n" + jar_path + " Packing Finished.\n")


if __name__ == "__main__":
    pack_mine(SRC_PATH, TEMP_PATH, JAR_PATH, ENTRY_NAME)
