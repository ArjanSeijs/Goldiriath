package net.goldiriath.plugin.dialog.script;

import net.goldiriath.plugin.quest.action.Action;

public abstract class ScriptItem implements Action {

    protected final Script script;
    protected int delay;

    public ScriptItem(Script script) {
        this.script = script;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Script getScript() {
        return script;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

}
