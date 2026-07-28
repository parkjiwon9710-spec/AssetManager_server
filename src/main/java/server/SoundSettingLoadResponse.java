package server;

import model.SoundSetting;

public class SoundSettingLoadResponse {
    public String type = "SOUND_SETTING_LOAD_RESPONSE";
    public SoundSetting setting;

    public SoundSettingLoadResponse(SoundSetting setting) {
        this.setting = setting;
    }
}
