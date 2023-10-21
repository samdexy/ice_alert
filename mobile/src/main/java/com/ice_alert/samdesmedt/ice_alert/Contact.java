package com.ice_alert.samdesmedt.ice_alert;


public class Contact {

    private String _name, _phone;
    private int _ranking;
    private int _id;

    public Contact(int id, String name, String phone, int ranking ) {
        _id = id;
        _name = name;
        _phone = phone;
        _ranking = ranking;

    }

    public int getId() { return _id; }

    public String getName() {
        return _name;
    }

    public String getPhone() {
        return _phone;
    }

    public int getRanking() { return _ranking; }


    public void setName(String newName) {
        _name = newName;
    }

    public void setPhone(String newPhone) {
        _phone = newPhone;
    }

    public void setRanking(int newRanking) {
        _ranking = newRanking;
    }


}
