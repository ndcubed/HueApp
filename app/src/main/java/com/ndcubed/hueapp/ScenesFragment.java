package com.ndcubed.hueapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import com.ndcubed.nappsupport.views.SimpleColorButton;
import com.ndcubed.nappsupport.views.WhiteDialog;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.listener.PHSceneListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHScene;
import com.ndcubed.hueapp.R;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by Nathan on 7/26/2015.
 */
public class ScenesFragment extends Fragment {

    View rootView;
    SceneControlArrayAdapter adapter;

    final int CREATE_SCENE_INTENT = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.scenes_fragment_layout, container, false);

        GridView gridView = (GridView)rootView.findViewById(R.id.scenesGridView);
        gridView.setPadding(gridView.getPaddingLeft(), gridView.getPaddingTop(), gridView.getPaddingRight(), (int)(com.ndcubed.nappsupport.utils.Common.getSoftButtonHeight(getActivity()) + Common.dpToPx(getActivity(), 10f)));

        adapter = new SceneControlArrayAdapter(getActivity(), R.layout.scene_list_item);
        gridView.setAdapter(adapter);

        PHHueSDK sdk = PHHueSDK.getInstance();
        final PHBridge bridge = sdk.getSelectedBridge();
        List<PHScene> scenes = bridge.getResourceCache().getAllScenes();

        for(PHScene scene : scenes) {
            if(!scene.getName().startsWith("hueDel")) {
                adapter.insert(new SceneItem(scene), 0);
            }
        }


        adapter.notifyDataSetChanged();

        adapter.setOnSceneClickListener(new SceneControlArrayAdapter.OnSceneClickListener() {
            @Override
            public void onClick(PHScene scene) {
                PHHueSDK.getInstance().getSelectedBridge().activateScene(scene.getSceneIdentifier(), "0", new PHSceneListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(int i, String s) {

                    }

                    @Override
                    public void onScenesReceived(List<PHScene> list) {

                    }

                    @Override
                    public void onSceneReceived(PHScene phScene) {

                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                    }
                });
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(adapter.getItem(position).getScene().getSceneIdentifier() + " ID");
                System.out.println(adapter.getItem(position).getScene().getName() + "NAME");
                PHHueSDK.getInstance().getSelectedBridge().activateScene(adapter.getItem(position).getScene().getSceneIdentifier(), "0", new PHSceneListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(int i, String s) {

                    }

                    @Override
                    public void onScenesReceived(List<PHScene> list) {

                    }

                    @Override
                    public void onSceneReceived(PHScene phScene) {

                    }

                    @Override
                    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

                    }
                });
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final SceneItem sceneItem = adapter.getItem(position);

                final WhiteDialog dialog = new WhiteDialog(getActivity());
                dialog.setMessageText("Delete " + sceneItem.getScene().getName() + "?");
                dialog.setPositiveButtonText("Delete");
                dialog.setDismissButtonText("Cancel");
                dialog.setHideOnClick(false);
                dialog.addDialogListener(new WhiteDialog.DialogListener() {
                    @Override
                    public void dismissButtonClicked() {
                        dialog.hide();
                    }

                    @Override
                    public void positiveButtonClicked() {
                        dialog.setIsLoadingDialog(true);
                        dialog.setMessageText("Talking With Bridge");

                        adapter.remove(adapter.getItem(position));
                        adapter.notifyDataSetChanged();
                        sceneItem.getScene().setName("hueDel" + sceneItem.getScene().getSceneIdentifier());
                        bridge.deleteScene(sceneItem.getScene().getSceneIdentifier(), new PHSceneListener() {
                            @Override
                            public void onScenesReceived(List<PHScene> list) {
                                System.out.println("ERRRRR: ");

                            }

                            @Override
                            public void onSceneReceived(PHScene phScene) {
                                System.out.println("ERRRR");
                            }

                            @Override
                            public void onSuccess() {
                                System.out.println("ERRRRR: ");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refresh();
                                        dialog.hide();
                                    }
                                });
                            }

                            @Override
                            public void onError(int i, String s) {
                                System.out.println("ERRRRR: " + s);
                            }

                            @Override
                            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                                System.out.println("ERRRRR: ");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refresh();
                                        dialog.hide();
                                    }
                                });
                            }
                        });
                    }
                });
                dialog.show();

                return true;
            }
        });

        ((SimpleColorButton)rootView.findViewById(R.id.newSceneButton)).setSimpleButtonListener(new SimpleColorButton.SimpleButtonListener() {
            @Override
            public void onClick(SimpleColorButton view) {

                startActivityForResult(new Intent(getActivity(), CreateSceneActivity.class), CREATE_SCENE_INTENT);

                /*
                final WhiteDialog dialog = new WhiteDialog(getActivity());
                dialog.setMessageText("Create a new scene. Type a name below, you'll find your new scene in the scenes tab.");
                dialog.setAcceptsInput(true);
                dialog.getInputField().setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                dialog.setHideOnClick(false);
                dialog.addDialogListener(new WhiteDialog.DialogListener() {
                    @Override
                    public void dismissButtonClicked() {
                        dialog.hide();
                    }

                    @Override
                    public void positiveButtonClicked() {
                        dialog.setIsLoadingDialog(true);
                        dialog.setMessageText("Talking With Bridge");

                        PHHueSDK sdk = PHHueSDK.getInstance();
                        PHBridge bridge = sdk.getSelectedBridge();

                        PHScene scene = new PHScene();
                        List<String> lightIDs = new ArrayList<>();
                        for(PHLight light : bridge.getResourceCache().getAllLights()) {
                            lightIDs.add(light.getIdentifier());
                        }
                        scene.setLightIdentifiers(lightIDs);
                        scene.setName(dialog.getInput());

                        bridge.saveSceneWithCurrentLightStates(scene, new PHSceneListener() {
                            @Override
                            public void onScenesReceived(List<PHScene> list) {
                            }

                            @Override
                            public void onSceneReceived(PHScene phScene) {
                            }

                            @Override
                            public void onSuccess() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        save();
                                        dialog.hide();
                                    }
                                });
                            }

                            @Override
                            public void onError(int i, String s) {
                            }

                            @Override
                            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        save();
                                        dialog.hide();
                                    }
                                });
                            }
                        });
                    }
                });

                dialog.show();
                */
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CREATE_SCENE_INTENT && resultCode == Activity.RESULT_OK) {

            String groupID = data.getStringExtra("groupID");
            String sceneName = data.getStringExtra("sceneName");
            System.out.println("GROUP ID" + groupID);
            System.out.println("SCENE NAME" + sceneName);

            PHHueSDK sdk = PHHueSDK.getInstance();
            PHBridge bridge = sdk.getSelectedBridge();
            List<PHGroup> groups = bridge.getResourceCache().getAllGroups();

            for(PHGroup group : groups) {

                if(group.getIdentifier().equals(groupID)) {

                    List<String> lightIDs = group.getLightIdentifiers();

                    final PHScene scene = new PHScene();
                    scene.setLightIdentifiers(lightIDs);
                    scene.setName(sceneName);

                    bridge.saveSceneWithCurrentLightStates(scene, new PHSceneListener() {
                        @Override
                        public void onScenesReceived(List<PHScene> list) {

                        }

                        @Override
                        public void onSceneReceived(PHScene phScene) {

                        }

                        @Override
                        public void onSuccess() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //save();
                                }
                            });
                        }

                        @Override
                        public void onError(int i, String s) {

                        }

                        @Override
                        public void onStateUpdate(final Map<String, String> map, List<PHHueError> list) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (Map.Entry entry : map.entrySet()) {
                                        String key = (String)entry.getKey();
                                        String id = (String)entry.getValue();

                                        scene.setSceneIdentifier(id);
                                        adapter.add(new SceneItem(scene));
                                        adapter.notifyDataSetChanged();

                                        System.out.println("VALUE: " + id);
                                        // For 1.11 Bridges the Scene ID is returned in the value (with key "id")
                                    }
                                }
                            });
                        }
                    });

                    break;
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        refresh();
    }

    public void refresh() {
        if(isAdded()) {
            PHHueSDK sdk = PHHueSDK.getInstance();
            PHBridge bridge = sdk.getSelectedBridge();
            List<PHScene> scenes = bridge.getResourceCache().getAllScenes();
            adapter.clear();
            for(PHScene scene : scenes) {
                if(!scene.getName().startsWith("hueDel")) {
                    adapter.insert(new SceneItem(scene), 0);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}
