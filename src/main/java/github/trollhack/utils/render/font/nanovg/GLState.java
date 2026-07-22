package github.trollhack.utils.render.font.nanovg;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public enum GLState {
    INSTANCE;

    private final int[] tex = new int[1];
    private final int[] prog = new int[1];
    private final int[] vao = new int[1];
    private final int[] vbo = new int[1];
    private final int[] viewport = new int[4];
    private final int[] blendSrc = new int[1];
    private final int[] blendDst = new int[1];
    private final int[] blendEq = new int[1];

    private boolean blendOn;
    private boolean depthOn;
    private boolean cullOn;
    private boolean scissorOn;

    public void push() {
        glGetIntegerv(GL_TEXTURE_BINDING_2D, tex);
        glGetIntegerv(GL_CURRENT_PROGRAM, prog);
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, vao);
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, vbo);
        glGetIntegerv(GL_VIEWPORT, viewport);
        glGetIntegerv(GL_BLEND_SRC_RGB, blendSrc);
        glGetIntegerv(GL_BLEND_DST_RGB, blendDst);
        glGetIntegerv(GL_BLEND_EQUATION_RGB, blendEq);

        blendOn = glIsEnabled(GL_BLEND);
        depthOn = glIsEnabled(GL_DEPTH_TEST);
        cullOn = glIsEnabled(GL_CULL_FACE);
        scissorOn = glIsEnabled(GL_SCISSOR_TEST);
    }

    public void pop() {
        glUseProgram(prog[0]);
        glBindTexture(GL_TEXTURE_2D, tex[0]);
        glBindVertexArray(vao[0]);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);

        glBlendEquation(blendEq[0]);
        glBlendFunc(blendSrc[0], blendDst[0]);

        toggleState(GL_BLEND, blendOn);
        toggleState(GL_DEPTH_TEST, depthOn);
        toggleState(GL_CULL_FACE, cullOn);
        toggleState(GL_SCISSOR_TEST, scissorOn);

        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }

    private void toggleState(int cap, boolean enable) {
        if (enable) glEnable(cap);
        else glDisable(cap);
    }
}
