package com.example.sarthi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Attendant_Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout new_drawer_Layout;
    private ActionBarDrawerToggle mtoggle;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new_drawer_Layout = findViewById(R.id.new_drawer_layout);


        mtoggle = new ActionBarDrawerToggle(this, new_drawer_Layout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        new_drawer_Layout.addDrawerListener(mtoggle);
        mtoggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView) findViewById(R.id.new_view);
        navigationView.setNavigationItemSelectedListener(this);
        recyclerView = findViewById(R.id.recyclerview);
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new new_home()).commit();
            //Intent intent=new Intent(this,HomedrawerActivity.class);
            //startActivity(intent);
            navigationView.setCheckedItem(R.id.new_home);
        }

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mtoggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new new_home()).commit();
                break;
            case R.id.new_my_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new new_myprofile()).commit();
                break;

            case R.id.new_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new new_settings()).commit();
                break;

            case R.id.new_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(Attendant_Home.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;

        }


        new_drawer_Layout.closeDrawer(GravityCompat.START);
        return true;
    }

}