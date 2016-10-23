package com.example.ayou7995.nagging;

/**
 * Created by ayou7995 on 2016/10/20.
 */
public class LobbyRow {

    private final static String Tag = "Jonathan";

    private String _name;
    private String _state;
    private String _subject;

    public LobbyRow() {
        _name = "";
        _state = "";
        _subject = "";
    }

    public void set_name(String name) { _name = name; }
    public void set_state(String state) { _state = state; }
    public void set_subject(String subject) { _subject = subject; }

    public String get_name() { return _name; }
    public String get_state() { return _state; }
    public String get_subject() { return _subject; }

}
