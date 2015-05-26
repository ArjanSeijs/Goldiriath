package me.dirkjan.goldiriath.quest.requirement;

import me.dirkjan.goldiriath.Goldiriath;
import me.dirkjan.goldiriath.Message;
import me.dirkjan.goldiriath.quest.Quest;
import me.dirkjan.goldiriath.quest.stage.Stage;
import org.bukkit.entity.Player;

public class QuestStartedRequirement extends AbstractRequirement {

    private final Quest quest;
    private final Stage stage;

    public QuestStartedRequirement(Goldiriath plugin, String args[]) {
        super(plugin, Message.QUEST_QUEST_NOT_STARTED);
        quest = new Quest(plugin.qm, args[1]);
        stage = quest.getEntryStage();
    }

    @Override
    public boolean has(Player player) {
        return plugin.pm.getData(player).getQuestData().getStage(quest).equals(stage);
    }

}
