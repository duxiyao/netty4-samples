package com.server.im.client.sdk;

import com.server.im.model.PkgInfo;

public interface IMOnReceive {
    void onReceive(PkgInfo pkgInfo);
}
