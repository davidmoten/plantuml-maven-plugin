package com.github.davidmoten.plantuml.plugins;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class GenerateTest {
    
    @Test
    public void testGeneratedImagesExist() {
        assertTrue(new File("target/generated-diagrams/one.png").exists());
        assertTrue(new File("target/generated-diagrams/two.png").exists());
        assertTrue(new File("target/generated-diagrams/two.svg").exists());
    }

}
