package github.trollhack.utils.render.shader.satin.impl;

import java.util.List;

public interface SamplerAccess {

    boolean hasSampler(String name);

    List<String> getSamplerNames();

    List<Integer> getSamplerShaderLocs();
}
