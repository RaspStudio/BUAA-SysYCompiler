package backend.value.data;

import backend.translate.MipsMapper;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsLabel;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.constant.data.ConstantDataArray;
import llvmir.tree.value.user.constant.data.ConstantInt;
import llvmir.tree.value.user.constant.data.ConstantString;
import llvmir.tree.value.user.constant.global.GlobalVariable;

import java.util.Collections;

public abstract class MipsData extends MipsLabel {

    protected MipsData(String name) {
        super(name);
    }

    public static MipsData create(GlobalVariable o, MipsMapper mapper) {
        String name = o.getPureName();
        Value data = o.getData();
        MipsData mipsData;
        if (data instanceof ConstantString) {
            mipsData = new MipsString(name, ((ConstantString) data).getContent());
        } else if (data instanceof ConstantInt) {
            mipsData = new MipsInts(name, Collections.singletonList(((ConstantInt) data).getValue()));
        } else if (data instanceof ConstantDataArray) {
            mipsData = new MipsInts(name, ((ConstantDataArray) data).getValues());
        } else {
            throw new RuntimeException("Unknown Data Type");
        }
        mapper.putAddr(o, MipsAddr.of(mipsData, null, null));
        return mipsData;
    }
}
