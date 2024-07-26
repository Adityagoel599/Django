package com.example.django.listeners;

import com.example.django.models.User;

public interface UsersListener {
    void initiateVideoMeeting(User user);
    void intitateAudioMeeting(User user);
}
