package com.example.medihelp;

public class Message{
private String role="user";
private String content;
public String getRole() {
    return role;
}

public void setRole(String role) {
    this.role = role;
}

public String getContent() {
    return content;
}

public void setContent(String messages) {
        String age = MainActivity.currentUserData.getAge();
        if(age==null)age="18";
        String gender =MainActivity.currentUserData.getGender();
        if(gender==null)gender="Female";

        this.content = "You are an AI assistant that is an expert in medical health. You know about symptoms and signs of various types of illnesses. Recommend the medical history queries with which specialist they should consult.  Format any lists on individual lines with a dash and a space in front of each line.I am a " + age + " year old " + gender + ".My symptoms are " + messages + ".Just give one word answer.";
    }

}
