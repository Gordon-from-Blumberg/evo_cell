package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.gordonfromblumberg.games.core.common.utils.DateTimeFormatter;
import com.gordonfromblumberg.games.core.common.utils.Paths;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.function.Consumer;

import static com.gordonfromblumberg.games.core.common.utils.FileUtils.getNameWithoutExtension;
import static com.gordonfromblumberg.games.core.common.utils.StringUtils.isBlank;

public class FileTable extends Table {
    private static final float MODIFIED_COL_WIDTH = 155f;
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(false);

    private final ClickListener onPathClickListener = new ClickListener(Input.Buttons.LEFT) {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);

            Label target = (Label) event.getListenerActor();
            if (target.getUserObject() instanceof File newDir) {
                open(newDir);
                event.stop();
            }
        }
    };

    private final ClickListener onFileRowClickListener = new ClickListener(Input.Buttons.LEFT) {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (event.getTarget() instanceof Label && event.getTarget().getUserObject() instanceof FileRow fileRow) {
                int clickCount = getTapCount();
                if (clickCount == 1) {
                    select(fileRow);
                } else if (clickCount == 2 && fileRow == selected) {
                    if (action != null) {
                        action.accept(selected.file);
                    }
                    setTapCount(0);
                }
            }
        }
    };

    private final Consumer<File> defaultAction = file -> {
        if (file.isDirectory()) {
            open(file);
        }
    };

    private final HorizontalGroup pathWidget = new HorizontalGroup().wrap(false).reverse();
    private final Table content = new Table();
    private File currentDir;
    private FileRow selected;
    private String extension;
    private boolean showDirectories;
    private Consumer<File> action;
    private FileTableStyle style;

    public FileTable(Skin skin) {
        this(skin, Paths.workDir().file(), null);
    }

    public FileTable(Skin skin, File currentDir, Consumer<File> action) {
        super(skin);

        add(pathWidget).colspan(2).expandX().fillX();

        row().spaceTop(15f);
        add("Name").expandX().align(Align.center).spaceRight(5f);
        add("Modified").width(MODIFIED_COL_WIDTH).align(Align.center);

        row();
        add(new VerticalScrollPane(content, skin, 300f)).colspan(2).expand().fill();
        content.defaults().spaceTop(2f).align(Align.left);
        content.columnDefaults(1).width(MODIFIED_COL_WIDTH);
        content.addListener(onFileRowClickListener);

        if (!isBlank(extension)) {
            this.extension = '.' + extension;
        }

        setCurrentDir(currentDir);
        this.action = action != null ? action : defaultAction;

        setStyle(skin.get(FileTableStyle.class));
    }

    public void open() {
        if (currentDir != null) {
            open(currentDir);
        }
    }

    public void open(File directory) {
        setCurrentDir(directory);

        fillPathWidget();
        fillContent();
        content.invalidate();
    }

    void fillPathWidget() {
        pathWidget.clear();

        File dir = currentDir;
        while (dir != null) {
            File parent = dir.getParentFile();
            if (parent != null) {
                Label label = new Label(dir.getName() + "/", getSkin());
                if (dir != currentDir) {
                    label.setUserObject(dir);
                    label.addListener(onPathClickListener);
                }
                pathWidget.addActor(label);
            }
            dir = parent;
        }

        File[] roots = File.listRoots();
        if (roots.length > 1) {
            SelectBox<File> selectBox = new SelectBox<>(getSkin());
            selectBox.setItems(roots);
            pathWidget.addActor(selectBox);

        } else {
            File root = roots[0];
            Label rootLabel = new Label(root.getPath(), getSkin());
            if (root != currentDir) {
                rootLabel.setUserObject(root);
                rootLabel.addListener(onPathClickListener);
            }
            pathWidget.addActor(rootLabel);
        }
    }

    void fillContent() {
        content.clearChildren();
        File[] files = currentDir.listFiles(this::showFile);
        if (files == null) {
            content.add("Error");
            return;
        }

        Label.LabelStyle nameStyle = getNameStyle();
        Label.LabelStyle modifiedStyle = getModifiedStyle();

        for (File file : files) {
            content.row();
            Label nameLabel = new Label(getNameWithoutExtension(file), nameStyle);
            Label modifiedLabel = new Label(dateTimeFormatter.format(file.lastModified()), modifiedStyle);
            FileRow fileRow = new FileRow(file, nameLabel, modifiedLabel);
            nameLabel.setUserObject(fileRow);
            modifiedLabel.setUserObject(fileRow);
            content.add(nameLabel).spaceRight(5f);
            content.add(modifiedLabel);
        }
    }

    private void setCurrentDir(File directory) {
        try {
            this.currentDir = directory.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't to get canonical path of directory " + directory, e);
        }
    }

    public FileTableStyle getStyle() {
        return style;
    }

    public void setStyle(FileTableStyle style) {
        this.style = style;

        setBackground(style.background);
    }

    public void setShowDirectories(boolean showDirectories) {
        this.showDirectories = showDirectories;
    }

    private boolean showFile(File file) {
        return file.isDirectory() ? showDirectories
                : isBlank(extension) || file.getName().endsWith(extension);
    }

    private void select(FileRow fileRow) {
        if (fileRow != selected) {
            if (selected != null) {
                unselect();
            }
            fileRow.nameLabel.setStyle(getSelectedNameStyle());
            fileRow.lastModifiedLabel.setStyle(getSelectedModifiedStyle());
            selected = fileRow;
        }
    }

    private void unselect() {
        if (selected != null) {
            selected.nameLabel.setStyle(getNameStyle());
            selected.lastModifiedLabel.setStyle(getModifiedStyle());
            selected = null;
        }
    }

    private Label.LabelStyle getNameStyle() {
        return style != null && style.nameStyle != null
                ? style.nameStyle : getSkin().get(Label.LabelStyle.class);
    }

    private Label.LabelStyle getSelectedNameStyle() {
        return style != null && style.selectedNameStyle != null
                ? style.selectedNameStyle : getSkin().get(Label.LabelStyle.class);
    }

    private Label.LabelStyle getModifiedStyle() {
        return style != null && style.modifiedStyle != null
                ? style.modifiedStyle : getSkin().get(Label.LabelStyle.class);
    }

    private Label.LabelStyle getSelectedModifiedStyle() {
        return style != null && style.selectedModifiedStyle != null
                ? style.selectedModifiedStyle : getSkin().get(Label.LabelStyle.class);
    }

    public static class FileTableStyle {
        public Drawable background;
        public Label.LabelStyle nameStyle;
        public Label.LabelStyle selectedNameStyle;
        public Label.LabelStyle modifiedStyle;
        public Label.LabelStyle selectedModifiedStyle;
    }

    private static record FileRow(File file, Label nameLabel, Label lastModifiedLabel) { }
}
