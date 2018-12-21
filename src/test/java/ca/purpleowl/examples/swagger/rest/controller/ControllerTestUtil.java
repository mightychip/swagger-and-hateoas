package ca.purpleowl.examples.swagger.rest.controller;

import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

@Log
class ControllerTestUtil {
    static String loadFromFile(String filePath) {
        try {
            Path path = Paths.get(
                Objects.requireNonNull(
                    ControllerTestUtil.class.getClassLoader().getResource(filePath)
                ).toURI()
            );

            return Files.lines(path).collect(Collectors.joining("\n"));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
