package org.stt.gui;

import dagger.Component;
import net.engio.mbassy.bus.MBassador;
import org.stt.BaseModule;
import org.stt.I18NModule;
import org.stt.command.CommandModule;
import org.stt.config.ConfigModule;
import org.stt.config.YamlConfigService;
import org.stt.event.EventBusModule;
import org.stt.event.ItemLogService;
import org.stt.fun.AchievementModule;
import org.stt.fun.AchievementService;
import org.stt.gui.jfx.JFXModule;
import org.stt.gui.jfx.STTApplication;
import org.stt.gui.jfx.WorktimePaneBuilder;
import org.stt.persistence.BackupCreator;
import org.stt.persistence.stt.STTPersistenceModule;
import org.stt.text.TextModule;
import org.stt.time.TimeUtilModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {TimeUtilModule.class, STTPersistenceModule.class, I18NModule.class,
        EventBusModule.class, TextModule.class, JFXModule.class, BaseModule.class, ConfigModule.class,
        AchievementModule.class, CommandModule.class})
public interface UIApplication {
    MBassador<Object> eventBus();

    YamlConfigService configService();

    BackupCreator backupCreator();

    AchievementService achievementService();

    ItemLogService itemLogService();

    STTApplication sttApplication();

    WorktimePaneBuilder worktimePaneBuilder();
}
