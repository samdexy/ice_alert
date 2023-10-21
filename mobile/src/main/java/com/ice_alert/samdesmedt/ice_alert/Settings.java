package com.ice_alert.samdesmedt.ice_alert;

/**
 * Created by samdesmedt on 16/04/2017.
 */

public class Settings {

    private String _answerAvailable, _answerUnavailable, _message;
    private int _id;

    public Settings(int id, String answerAvailable, String answerUnavailable, String message ) {
        _id = id;
        _answerAvailable = answerAvailable;
        _answerUnavailable = answerUnavailable;
        _message = message;

    }

    public int getId() { return _id; }

    public String getAnswerAvailable() {
        return _answerAvailable;
    }

    public String getAnswerUnavailable() { return _answerUnavailable; }

    public String getMessage() { return _message; }


    public void setAnswerAvailable(String newAnswerAvailable) {
        _answerAvailable = newAnswerAvailable;
    }

    public void setAnswerUnavailable(String newAnswerUnavailable) {
        _answerUnavailable = newAnswerUnavailable;
    }

    public void setMessage(String newMessage) {
        _message = newMessage;
    }

}
