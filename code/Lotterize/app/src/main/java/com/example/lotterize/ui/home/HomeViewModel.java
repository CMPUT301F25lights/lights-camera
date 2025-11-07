package com.example.lotterize.ui.home;

import android.text.Html;
import android.text.Spanned;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


/**
 * Holds Text for info dialog fragment.
 */
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Spanned> mText;

    /**
     * Constructor for HomeViewModel
     */
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(Html.fromHtml(
        "<b><u>How to get selected for events:</u></b> <br>" +
        "1. Click on the event. <br>" +
        "2. Click join waiting list. (Make sure registration deadline hasn't already passed!) <br>" +
        "3. Wait. (You will get a notification telling you if you are chosen or not) <br>" +
        "4. Once chosen go to 'Events Registered' (2nd from the right in the bottom bar) <br>" +
        "5. Accept the invitation <br>" +
        "6. Congragulations! You are now officially attending the event! <br>" +
        " <br>"+
        "<b><u>How participants are chosen:</u></b> <br>" +
        "    Each event has a total number of available positions (n) that can be filled. When the organizer clicks the button, " +
        "the system sends n invitations randomly to people on the waiting list. If anyone on the waiting list declines the " +
        "invitation, the system sends another random person on the waiting list a invitation. This is repeated until the total " +
        "number of available positions are filled. Good Luck!"
        ,Html.FROM_HTML_MODE_LEGACY));
    }

    /**
     * returns text with all guidelines information
     * @return LiveData<Spanned> - the text with guidelines information
     */
    public LiveData<Spanned> getText() {
        return mText;
    }
}