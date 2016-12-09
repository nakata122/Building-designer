package opengl.example.user.hideandseek;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

class Product
{

    public String name;
    public String quantity;
    public String color;

}

public class MainActivity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;
    static int pro;
    String[] names = {"Walls", "Patterns", "Floor", "Doors", "Windows","Rooftops", "Furniture"};
    public static Bitmap bit2;
    public static Bitmap[] skyBox = new Bitmap[7], textures = new Bitmap[10];
    LoadModel loader;
    ListView list;
    Button btn, btn2, btn3;
    FrameLayout frame;
    ExtendFragment frag;
    private DrawerLayout rel;
    private static ProgressBar bar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rel = (DrawerLayout) findViewById(R.id.rel);
        rel.setScrimColor(Color.TRANSPARENT);
        frag = new ExtendFragment(this);
        getFragmentManager().beginTransaction().addToBackStack(null).commit();

        bar = (ProgressBar) findViewById(R.id.progressBar);


        frame = (FrameLayout) findViewById(R.id.content_frame);
        btn = (Button) findViewById(R.id.rotate);
        btn2 = (Button) findViewById(R.id.move);
        btn3 = (Button) findViewById(R.id.delete);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLView.rotate = true;
                mGLView.move = false;
                MyGLRenderer.mode = 1; //ROTATE
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLView.rotate = false;
                mGLView.move = true;
                MyGLRenderer.mode = 0; //MOVING
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLView.rotate = false;
                mGLView.move = false;
                MyGLRenderer.mode = 5; //DELETE
            }
        });
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        for(int i=0;i<5;i++) {
            textures[i] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("wall" + String.valueOf(i), "drawable", getPackageName()), options);
        }
        bit2 = BitmapFactory.decodeResource(getResources(),R.drawable.grass, options);

        for(int i=0;i<6;i++){
            skyBox[i] = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("skybox_image_" + String.valueOf(i), "drawable", getPackageName()), options);
        }

        list = (ListView) findViewById(R.id.drawer);
        list.setAdapter(new ArrayAdapter<String>(this,R.layout.item,names));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                ft.replace(R.id.btnHolder, frag);
                ft.show(frag);
                ft.commit();
                list.bringChildToFront(frag.v);
                frag.setSelection(names[position], position);
            }
        });
        rel.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                list.bringToFront();
                //rel.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                ft.hide(frag);
                ft.commit();

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction().addToBackStack(null).commit();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }

        });

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                loader = new LoadModel();
                //Mode 0 = Normal, 1 = Blueprint
                if(loader.load(MainActivity.this,"table.xml",0.4f)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.table_tex, options);
                    MainActivity.setProg(20);
                }
                if(loader.load(MainActivity.this,"table2.xml",0.20f)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.oak, options);
                    MainActivity.setProg(10);
                }
                if(loader.load(MainActivity.this,"chair1.xml",3)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.chair1, options);
                    MainActivity.setProg(20);
                }
                if(loader.load(MainActivity.this,"chair2.xml",22)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.dark_wood, options);
                    MainActivity.setProg(40);
                }
                if(loader.load(MainActivity.this,"sofa.xml",7)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.velvet, options);
                    MainActivity.setProg(50);
                }
                if(loader.load(MainActivity.this,"desk.xml",4)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.desk, options);
                    MainActivity.setProg(60);
                }
                if(loader.load(MainActivity.this,"wardrobe.xml",2)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.oak, options);
                    MainActivity.setProg(70);
                }
                if(loader.load(MainActivity.this,"door1.xml",2)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.desk, options);
                    MainActivity.setProg(80);
                }
                if(loader.load(MainActivity.this,"window1.xml",0.2f)) {
                    Model.textures[Model.br-1] = BitmapFactory.decodeResource(getResources(),R.drawable.window1tex, options);
                    MainActivity.setProg(80);
                }
//              Terrain ganeration
                int m=0;
                //Verts
                for (int i = -100; i <= 100; i+=10) {
                    for (int j = -100; j <= 100; j+=10) {
                        Terrain.verts[m] = j;
                        m++;
                        Terrain.verts[m] = i;
                        m++;
                        Terrain.verts[m] = 0;
                        m++;
                    }
                }
                m=0;
                for (int i = -10; i <= 10; i+=1) {
                    for (int j = -10; j <= 10; j+=1) {
                        Terrain.texc[m] = j;
                        m++;
                        Terrain.texc[m] = i;
                        m++;
                        Terrain.texc[m] = 0;
                        m++;
                    }
                }
                m=0;
                //indices
                for (short i = 400; i > 0; i--) {
                    if(i%20==0 && i!=0) {
                        Terrain.order[m] = i;
                        m++;
                        Terrain.order[m] = i;
                        m++;
                        Terrain.order[m] = (short) (i + 20);
                        m++;
                        Terrain.order[m] = (short) (i + 20);
                        m++;
                    }else {
                        Terrain.order[m] = i;
                        m++;
                        Terrain.order[m] = (short) (i + 20);
                        m++;
                    }
                }
                MainActivity.setProg(100);
                final Runnable runs = new Runnable() {
                    @Override
                    public void run() {
                        mGLView = new MyGLSurfaceView(MainActivity.this);
                        frame.addView(mGLView);

                    }

                };
                runOnUiThread(runs);
                return null;
            }
        };
        task.execute();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.animator.slide_out, R.animator.slide_out);
            ft.hide(frag);
            ft.commit();
            if(fm.getBackStackEntryCount()>=2) {}
            else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Do you want to exit");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        } else {
            super.onBackPressed();
        }
    }

    public static void setProg(int progress){
        bar.setProgress(progress);
        pro = progress;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        if(mGLView != null)
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        if(mGLView != null)
        mGLView.onResume();
    }
}
