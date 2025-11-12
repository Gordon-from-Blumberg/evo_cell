package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface Renderer extends Disposable {
    void render(float dt);
    void resize(int width, int height);
    OrthographicCamera getCamera();
    Viewport getViewport();

    /**
     * Преобразует экранные координаты в систему вьюпорта
     * @param x x в экранных координатах
     * @param y y в экранных координатах
     * @param out Вектор, в который запишется результат
     */
    void screenToViewport(float x, float y, Vector3 out);
    /**
     * Преобразует экранные координаты в систему вьюпорта
     * @param coords Вектор с входными координатами, в него же запишется результат
     */
    void screenToViewport(Vector3 coords);
    /**
     * Преобразует координаты из системы вьюпорта в экранные
     * @param x x в координатной системе вьюпорта
     * @param y y в координатной системе вьюпорта
     * @param out Вектор, в который запишется результат
     */
    void viewportToScreen(float x, float y, Vector3 out);
    /**
     * Преобразует координаты из системы вьюпорта в экранные
     * @param coords Вектор с входными координатами, в него же запишется результат
     */
    void viewportToScreen(Vector3 coords);
}
