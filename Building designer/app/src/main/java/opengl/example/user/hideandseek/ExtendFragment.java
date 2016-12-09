package opengl.example.user.hideandseek;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.zip.Inflater;


public class ExtendFragment extends Fragment{
    View v;
    ListView list;
    int selected;
    String[] names = {"Walls", "Patterns", "Floor", "Doors", "Windows","Rooftops", "Furniture"};
    String[] wallTexture = {"wall0", "wall1", "wall2", "wall3","wall4"};
    String[] walls = {"wallline","wallsquare"};
    String[] doors = {"door1thumb"};
    String[] windows = {"window1thumb"};
    String[] furniture = {"table1thumb","table2thumb","chair1thumb","chair2thumb","sofa1thumb","desk1thumb","wardrobe1thumb"};
    Context ctx;
    Adapter adapter = new Adapter();

    public ExtendFragment(Context context){
        ctx = context;
    }

    public void setSelection(String n, int sel){
        selected = sel;
        switch (n){
            case "Walls":
                adapter.setCtx(ctx, walls);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
            case "Patterns":
                adapter.setCtx(ctx, wallTexture);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
            case "Floor":
                adapter.setCtx(ctx, walls);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
            case "Doors":
                adapter.setCtx(ctx, doors);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
            case "Windows":
                adapter.setCtx(ctx, windows);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
            case "Rooftops":
                adapter.setCtx(ctx, walls);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
            case "Furniture":
                adapter.setCtx(ctx, furniture);
                if(list!=null)
                    list.setAdapter(adapter);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag, container, false);
        list = (ListView) v.findViewById(R.id.expans);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(selected){
                    case 0:
                        if(position==0) MyGLRenderer.mode = 3; //  WALLS
                        break;
                    case 1:
                        MyGLRenderer.mode = 6; //  TEXTURING
                        MyGLRenderer.paitTex=position;
                        break;
                    case 3:
                        MyGLRenderer.items[position+furniture.length]=true; //  DOORS
                        break;
                    case 4:
                        MyGLRenderer.items[position+furniture.length+doors.length]=true; //  DOORS
                        break;
                    case 6:
                        MyGLRenderer.items[position]=true; //  FURNITURE
                        break;
                }
            }
        });
        v.bringToFront();
        list.bringToFront();
        list.requestLayout();
        return v;
    }
}
