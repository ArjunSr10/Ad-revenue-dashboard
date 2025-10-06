package main.dash.event;

import main.dash.enums.SceneName;

public interface SceneListener {
    /**
     * the callback of scene changed
     * @param sceneName the SceneName  type  of new scene
     */
    public void sceneChanged(SceneName sceneName);
}
