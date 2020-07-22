package com.App.RnineT;

import java.util.List;

public interface RnineTDrive {
    public boolean download(String token, String directoryID, String downloadDirectoryPath);
    public boolean upload(String token, String path);
}
