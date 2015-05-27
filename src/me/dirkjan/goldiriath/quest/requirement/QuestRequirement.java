package me.dirkjan.goldiriath.quest.requirement;

import me.dirkjan.goldiriath.Goldiriath;
import me.dirkjan.goldiriath.Message;
import me.dirkjan.goldiriath.quest.ParseException;
import me.dirkjan.goldiriath.quest.Quest;
import me.dirkjan.goldiriath.quest.stage.Stage;
import org.bukkit.entity.Player;

public class QuestRequirement extends AbstractRequirement {

    private final Quest quest;
    private final Stage stage;

    public QuestRequirement(Goldiriath plugin, String args[]) {
        super(plugin, Message.QUEST_QUEST_NOT_DONE);
        quest = plugin.qm.getQuest(args[1]);
        if (quest == null) {
            throw new ParseException("Quest '" + args[1] + "' not found.");
        }
        if (args[2].contains("started")) {
            stage = null;
        } else {
            stage = quest.getStage(args[2]);
            if (stage == null) {
                throw new ParseException("Stage '" + args[2] + "' not found.");
            }
        }

    }

    @Override
    public boolean has(Player player) {
        Stage questStage = plugin.pm.getData(player).getQuestData().getStage(quest);
        return stage == null ? questStage != null : (questStage == null ? false : questStage == stage);

    }

}
