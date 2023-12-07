package com.sy.cc.uitl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DokcerInfoUitl {




    public static String getContainerIPCmd(String containerId) throws IOException {
        Process process = Runtime.getRuntime().exec("docker inspect --format '{{ .NetworkSettings.IPAddress }}' " + containerId);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String containerIP = reader.readLine();
            return containerIP;
        }
    }
}
