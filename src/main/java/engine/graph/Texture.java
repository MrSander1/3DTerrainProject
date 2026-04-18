package engine.graph;

import java.io.InputStream;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private int textureId;
    private String texturePath;

    public Texture(int width, int height, ByteBuffer buf) {
        this.texturePath = "";
        generateTexture(width, height, buf);
    }

    public Texture(String resourcePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.texturePath = resourcePath;
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer buffer;
            try (InputStream is = Texture.class.getResourceAsStream("/" + resourcePath)) {
                if (is == null) {
                    throw new RuntimeException("Image file [" + resourcePath + "] not found.");
                }
                byte[] bytes = is.readAllBytes();
                buffer = MemoryUtil.memAlloc(bytes.length);
                buffer.put(bytes).flip();
            } catch (IOException e) {
                throw new RuntimeException("Error reading texture stream", e);
            }


            ByteBuffer buf = stbi_load_from_memory(buffer, w, h, channels, 4);
            MemoryUtil.memFree(buffer);

            if (buf == null) {
                throw new RuntimeException("Image file [" + resourcePath + "] not loaded: " + stbi_failure_reason());
            }

            int width = w.get();
            int height = h.get();

            generateTexture(width, height, buf);

            stbi_image_free(buf);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }

    private void generateTexture(int width, int height, ByteBuffer buf) {
        textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public String getTexturePath() {
        return texturePath;
    }
}
