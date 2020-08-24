package com.RnineT.Transfer.Drives.Box;

public class Item<ItemObj, ItemInfo>{
    public static final String FILE = "file";
    public static final String FOLDER = "folder";
    public String type;
    public ItemObj item;
    public ItemInfo itemInfo;
    public Item(String type, ItemObj itemType, ItemInfo itemInfo){
        this.type = type;
        this.itemInfo = itemInfo;
        this.item = itemType;
    }
}
