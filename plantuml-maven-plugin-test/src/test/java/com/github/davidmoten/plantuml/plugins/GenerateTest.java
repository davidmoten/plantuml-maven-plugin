package com.github.davidmoten.plantuml.plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class GenerateTest {
    
    @Test
    public void testGeneratedImagesExist() {
        assertTrue(new File("target/generated-diagrams/one.png").exists());
        assertTrue(new File("target/generated-diagrams/two.png").exists());
        assertTrue(new File("target/generated-diagrams/two.svg").exists());
        assertTrue(new File("target/generated-diagrams/two_001.preproc").exists());
    }
    
    @Test
    public void testPreserveDirectoryStructureAndOutputSvgOnly() {
        assertTrue(new File("target/generated-diagrams/three/three.svg").exists());
        assertTrue(new File("target/generated-diagrams/four/four.svg").exists());
        assertFalse(new File("target/generated-diagrams/three/three.png").exists());
        assertFalse(new File("target/generated-diagrams/four/four.png").exists());
    }

}
