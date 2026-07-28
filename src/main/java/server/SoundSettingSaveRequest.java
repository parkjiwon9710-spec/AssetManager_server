package server;

import model.SoundSetting;

public class SoundSettingSaveRequest {
    public String type = "SOUND_SETTING_SAVE_REQUEST";
    public int userId;
    public SoundSetting setting;

    public SoundSettingSaveRequest(int userId, SoundSetting setting) {
        this.userId = userId;
        this.setting = setting;
    }
}
