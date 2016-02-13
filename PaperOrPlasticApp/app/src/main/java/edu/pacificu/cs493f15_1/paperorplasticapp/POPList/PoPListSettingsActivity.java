package edu.pacificu.cs493f15_1.paperorplasticapp.POPList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import edu.pacificu.cs493f15_1.paperorplasticapp.Menu.SettingsActivity;
import edu.pacificu.cs493f15_1.paperorplasticapp.R;
import edu.pacificu.cs493f15_1.paperorplasticjava.PoPLists;

/**
 * Created by heyd5159 on 2/6/2016.
 */
/***************************************************************************************************
 *   Class:         GroceryListSettingsActivity
 *   Description:   Creates GroceryListSettingsActivity class that controls what occurs when the
 *                  user reaches the grocery list settings page. Specifically, handles what happens
 *                  when the user specifies whether the grocery list button should be displayed on
 *                  the continue activity.
 *                  creates intents that take users to those specific pages.
 *   Parameters:    N/A
 *   Returned:      N/A
 **************************************************************************************************/

public class PoPListSettingsActivity extends FragmentActivity implements View.OnClickListener
{
    final float SLIDE_RIGHT_ITEM = 5;
    final float SLIDE_LEFT_ITEM = -145;

    private ListView mListOfListView;
    private PoPLists mPoPLists;
    private PoPListAdapter mListAdapter;
    private DeleteListDialogListener mDeleteListListener;
    private Button mbBack;
    private Button mbEdit;
    private boolean mbIsOnEdit;
    private FragmentManager fm;
    int position = 0;
    Button delete;

    private String mPoPFileName;


    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    protected void PoPOnCreate (Bundle savedInstanceState, PoPLists popLists, final int activitylayout, final String fileName)
    {
        setContentView(activitylayout);
        mbIsOnEdit = false;

        mPoPLists = popLists;
        mPoPFileName = fileName;

        //set up button
        mbEdit = (Button) findViewById (R.id.bEdit);
        mbEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if its clicked, show or hide delete buttons
                int size = mPoPLists.getSize();
                if (size > 0) {
                    if (!mbIsOnEdit) {
                        mbIsOnEdit = true;
                        for (int i = 0; i < size; i++) {
                            showDeleteButton(i);
                        }
                    } else {

                        //showDeleteButton also gets rid of the delete button so we might not need this check
                        //TODO might need to show again if tab is changed
                        mbIsOnEdit = false;
                        for (int i = 0; i < size; i++) {
                            hideDeleteButton(i);
                        }
                    }
                }
            }
        });

        /*Do we need a back button?*/
        mbBack = (Button) findViewById (R.id.bBack);
        mbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go back to activity that called this page (possible pages are settings
                // or grocery list page
                String caller = getIntent().getStringExtra("Caller");
                Intent intent;
                if (caller.equals("SettingsActivity")) {
                    intent = new Intent(PoPListSettingsActivity.this, SettingsActivity.class); //TODO Come back to this maybe if statements?
                } else {
                    intent = new Intent(PoPListSettingsActivity.this, PoPListActivity.class);//TODO Come back to this maybe if statements?
                }

                startActivity(intent);
            }
        });

//        Context context = getApplicationContext();
//        File groceryFile = context.getFileStreamPath(GroceryLists.GROCERY_FILE_NAME);


        //set up list view to view lists
        mListOfListView = (ListView) findViewById(R.id.listViewOfLists);
        //list adapter holds info of lists for listView
        mListAdapter = new PoPListAdapter(mListOfListView.getContext(),
                R.layout.listview_list_row_settings, mPoPLists.getArrayOfLists()) {
        };

        mListOfListView.setAdapter(mListAdapter);

        //set up swipe listening
        mListOfListView.setOnTouchListener(new OnSwipeTouchListener(this, mListOfListView) {
            @Override
            public void onSwipeRight(int pos) {


                if (!mbIsOnEdit) {
                    hideDeleteButton(pos);
                }

            }

            @Override
            public void onSwipeLeft(int pos) {

                if (!mbIsOnEdit) {
                    showDeleteButton(pos);
                }
            }
        });
    }


    public void onClick (View view)
    {
    }

    /********************************************************************************************
     * Function name: readListsFromFile
     *
     * Description:   Reads from the GROCERY_FILE_NAME the current GroceryLists
     *
     * Parameters:    None
     *
     * Returns:       None
     ******************************************************************************************/
    private void readListsFromFile (PoPLists popLists)
    {
        FileInputStream popInput;
        Scanner listsInput;

        try {
            popInput = openFileInput(mPoPFileName);

            listsInput = new Scanner(popInput);
            popLists.readListsFromFile(listsInput);
            listsInput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /********************************************************************************************
     * Function name: writeGListsToGroceryFile
     *
     * Description:   Writes the current mGLists to GROCERY_FILE_NAME to store the information
     *                stored in mGLists
     *
     * Parameters:    None
     *
     * Returns:       None
     ******************************************************************************************/
    private void writeListsToFile ()
    {
        FileOutputStream popOutput = null;
        PrintWriter listsOutput = null;

        try
        {
            popOutput = openFileOutput(mPoPFileName, Context.MODE_PRIVATE);

            listsOutput = new PrintWriter(popOutput);
            mPoPLists.writeListsToFile(listsOutput);
            listsOutput.flush();
            listsOutput.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /********************************************************************************************
     * Function name: showDeleteButton
     *
     * Description:   Shows the delete button for the child view within listView and sets the
     *                onClickListener for the delete button
     *
     * Parameters:    pos - the child position within the list view whose delete button will be
     *                      shown
     *
     * Returns:       true if the child view with the button being hidden exists, else false
     ******************************************************************************************/
    private boolean showDeleteButton(final int pos) {
        position = pos;
        View child = mListOfListView.getChildAt(pos - mListOfListView.getFirstVisiblePosition());
        if (child != null) {

            delete = (Button) child.findViewById(R.id.bDelete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    mDeleteListListener = new DeleteListDialogListener()
                    {
                        @Override
                        public void onDeleted ()
                        {
                            deleteList();
                        }
                    };
                    fm = getSupportFragmentManager();
                    DeletePoPListDFragment deleteListFragment = new DeletePoPListDFragment();
                    deleteListFragment.show(fm, "Yeah");

                }
            });
            if (delete != null)
            {
                if (delete.getVisibility() == View.INVISIBLE) {
                    Animation deleteAnimation =
                            AnimationUtils.loadAnimation(this,
                                    R.anim.slide_out_left);

                    delete.startAnimation(deleteAnimation);
                    delete.setVisibility(View.VISIBLE);

                    slideItemView(child, SLIDE_LEFT_ITEM);
                }
            }
            return true;
        }
        return false;
    }

    /********************************************************************************************
     * Function name: hideDeleteButton
     *
     * Description:   Hides the delete button on each list view child
     *
     * Parameters:    pos - the child position within the list view
     *
     * Returns:       true if the child view with the button being hidden exists, else false
     ******************************************************************************************/
    private boolean hideDeleteButton(final int pos) {
        position = pos;
        View child = mListOfListView.getChildAt(pos - mListOfListView.getFirstVisiblePosition());
        if (child != null) {

            delete = (Button) child.findViewById(R.id.bDelete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                }
            });
            if (delete != null)
            {
                if (delete.getVisibility() == View.VISIBLE) {
                    Animation deleteAnimation =
                            AnimationUtils.loadAnimation(this,
                                    R.anim.slide_in_right);

                    delete.startAnimation(deleteAnimation);

                    delete.setVisibility(View.INVISIBLE);

                    slideItemView(child, SLIDE_RIGHT_ITEM);

                }
            }
            return true;
        }
        return false;
    }

    /********************************************************************************************
     * Function name: slideItemView
     *
     * Description:   Slides the list view item over
     *
     * Parameters:    child             - the view that is sliding
     *                translationAmount - how much the view will slide
     *
     * Returns:       none
     ******************************************************************************************/

    private void slideItemView (View child, float translationAmount)
    {
        TextView listName = (TextView) child.findViewById(R.id.listName);
        listName.setTranslationX(translationAmount);

    }


    /********************************************************************************************
     * Function name: onResume
     *
     * Description:   When the activity is resumed reads in GroceryLists from GROCERY_FILE_NAME
     *                and updates mGLists with the information.
     *
     * Parameters:    none
     *
     * Returns:       none
     ******************************************************************************************/
    @Override
    protected void onResume ()
    {
        super.onResume();


        //read list info from file
        Context context = getApplicationContext();
        File groceryFile = context.getFileStreamPath(mPoPFileName);

        if (groceryFile.exists())
        {
            mPoPLists.clearLists();
            readListsFromFile(mPoPLists);
        }
    }

    /********************************************************************************************
     * Function name: onPause
     *
     * Description:   When the activity is paused writes the GroceryLists to groceryList.txt
     *
     * Parameters:    none
     *
     * Returns:       none
     ******************************************************************************************/
    @Override
    protected void onPause ()
    {
        super.onPause();

        writeListsToFile();
        mPoPLists.clearLists();

    }

    /********************************************************************************************
     * Function name: deleteList
     *
     * Description:   When the activity is paused writes the GroceryLists to groceryList.txt
     *
     * Parameters:    none
     *
     * Returns:       none
     ******************************************************************************************/
    public void deleteList ()
    {
        mPoPLists.deleteList(position);
        mListAdapter.notifyDataSetChanged();
    }

    /********************************************************************************************
     * Function name: DeleteListDialogListener
     *
     * Description:   returns the mDeleteListListener for other class to use
     *
     * Parameters:    none
     *
     * Returns:       mDeleteListListener
     ******************************************************************************************/
    public DeleteListDialogListener getDeleteDialogListener()
    {
        return mDeleteListListener;
    }

}
