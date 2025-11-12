package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class TextureAtlasPacker {
    public static void main(String[] args) {
        TexturePacker.process("../../core/resources/image", "image", "texture_pack");
        TexturePacker.process("../../core/resources/ui_image", "ui", "uiskin");
    }
}
