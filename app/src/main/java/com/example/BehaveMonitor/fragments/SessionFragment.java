//------------------------------------------------------------------------------
// Copyright (c) 2015 Barney Dennis & Gareth Lewis.
//------------------------------------------------------------------------------

package com.example.BehaveMonitor.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.BehaveMonitor.DBHelper;
import com.example.BehaveMonitor.FileHandler;
import com.example.BehaveMonitor.HomeActivity;
import com.example.BehaveMonitor.Observation;
import com.example.BehaveMonitor.R;
import com.example.BehaveMonitor.Session;
import com.example.BehaveMonitor.SessionActivity;
import com.example.BehaveMonitor.Template;
import com.example.BehaveMonitor.TemplateActivity;

import org.angmarch.circledpicker.CircledPicker;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionFragment extends Fragment {

    private View rootView;
    private DBHelper db;

    private String sessionName;
    private String sessionLocation;

    private File activeFolder;
    private String activeFolderName;

    private int nObservations = 1;
    private Observation observations;
    private Template activeTemplate;
    private String activeTemplateName;

	public SessionFragment() { }

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_session_create, container, false);
        final EditText nObsView = (EditText) rootView.findViewById(R.id.observations_amount);
        nObsView.clearFocus();

        db = DBHelper.getInstance(getActivity());

        HomeActivity homeActivity = (HomeActivity) getActivity();
        activeFolderName = homeActivity.getFolderName();
        activeTemplateName = homeActivity.getTemplateName();

        nObsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                int inType = nObsView.getInputType();
                nObsView.setInputType(InputType.TYPE_NULL);
                nObsView.onTouchEvent(event);
//                nObsView.setInputType(inType);
                return true;
            }
        });

        nObsView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    final View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
                    dialog.setView(dialogView);

                    final CircledPicker picker = ((CircledPicker) dialogView.findViewById(R.id.circled_picker));
                    picker.setValue(nObservations - 1);

                    final TextView nameLabel = (TextView) rootView.findViewById(R.id.name_label);

                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int n = (int) picker.getValue();
                            nObservations = n + 1;
                            nObsView.setText("" + nObservations);
                            nObsView.clearFocus();

                            if (nObservations == 1) {
                                nameLabel.setText(R.string.session_create_name);
                            } else {
                                nameLabel.setText(R.string.session_create_name_prefix);
                            }
                        }
                    });

                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            nObsView.clearFocus();
                        }
                    });

                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                                dialog.dismiss();
                                nObsView.clearFocus();
                            }

                            return false;
                        }
                    });

                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        });

        setFolderSpinner();
        setTemplateSpinner();
        setNewButtons();

        setCreateButton();

        return rootView;
	}

    private void readTemplatesNumber() {
        try {
            nObservations = Integer.parseInt(((EditText) rootView.findViewById(R.id.observations_amount)).getText().toString());
            if (nObservations < 1) {
                nObservations = 0;
                makeSomeToast("Minimum number of observations is 1");
            } else {
                if (nObservations > 10) {
                    nObservations = 0;
                    makeSomeToast("Maximum number of observations is 10");
                }
            }
        } catch (NumberFormatException e) {
            nObservations = 0;
            makeSomeToast("Must enter a number of observations (1 - 10)");
        }
    }

	private void setNewButtons() {
		ImageButton newFolderButton = (ImageButton) rootView.findViewById(R.id.new_folder);
		newFolderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				newFolder(getActivity());
			}
		});

        ImageButton newTemplateButton = (ImageButton) rootView.findViewById(R.id.new_template);
        newTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View v) {
                db.setFolder(activeFolderName);
                newTemplate(getActivity());
            }
        });
	}

	private void newFolder(final Context context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_folder, null);
        alert.setView(dialogView);
        final EditText input = (EditText) dialogView.findViewById(R.id.dialog_folder_name);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String folderName = input.getText().toString();
                folderName = folderName.trim();
                if (validateFolderName(folderName)) {
                    FileHandler.createNewFolder(folderName);
                    activeFolderName = folderName;
                    setFolderSpinner();
                }
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
		alert.show();
	}

    /**
     * Checks if the folder name has any invalid characters
     * @param name the name of the folder to validate
     * @return true if the folder name is valid, false otherwise
     */
    private boolean validateFolderName(String name) {
        if (name.equals("")) return false;
        String namePattern = "[a-zA-Z0-9_ ]+";
        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    private void newTemplate(final Context context) {
        Intent intent = new Intent(context, TemplateActivity.class);
        intent.putExtra("fromFragment", false);
        startActivity(intent);
    }

	private void setFolderSpinner() {
		String[] folders = FileHandler.getFolders();
        if (folders == null) {
            FileHandler.checkFoldersExist(getActivity());
            folders = FileHandler.getFolders();
        }

		Spinner spinner = (Spinner) rootView.findViewById(R.id.folder_spinner);

		// Create an ArrayAdapter using the string array and a default spinner layout
		Log.w("spinner", spinner.toString());
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
				android.R.layout.simple_spinner_item,
                folders);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

        if (!"".equals(activeFolderName)) {
            int i = 0;
            while (i < folders.length && i != -1) {
                if (folders[i].equals(activeFolderName)) {
                    spinner.setSelection(i);
                    i = -2;
                }

                i++;
            }
        }
	}

    private Spinner setTemplateSpinner() {
        String[] templates = FileHandler.getTemplateNames();
        if (templates == null || templates.length == 0) {
            templates = new String[]{"No templates exist."};
        } else {
            for (String s : templates) {
                Log.e("Behave", "Template name: " + s);
            }
        }

        Spinner spinner = (Spinner) rootView.findViewById(R.id.template_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, templates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (!"".equals(activeTemplateName)) {
            int i = 0;
            while (i < templates.length && i != -1) {
                if (templates[i].equals(activeTemplateName)) {
                    spinner.setSelection(i);
                    i = -2;
                }

                i++;
            }
        }

        return spinner;
    }

    private void setCreateButton() {
        Button button = (Button) rootView.findViewById(R.id.session_create_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveFolder();
                if (!validate()) {
                    return;
                }

                readTemplatesNumber();
                if (nObservations <= 0 || nObservations > 10) {
                    return;
                }

                if (loadSelectedTemplate()) {
                    createSession();
                }
            }
        });
    }

    private boolean validate() {
        sessionName = ((EditText) rootView.findViewById(R.id.session_name)).getText().toString();
        sessionLocation = ((EditText) rootView.findViewById(R.id.session_location)).getText().toString();

        Spinner spinner = (Spinner) rootView.findViewById(R.id.template_spinner);
        activeTemplateName = spinner.getSelectedItem().toString();

        if ("No templates exist.".equals(activeTemplateName)) {
            makeSomeToast("Must select a template");
            return false;
        }

        if ("".equals(sessionName)) {
            makeSomeToast("Must enter a name");
            return false;
        }

        if ("".equals(sessionLocation)) {
            makeSomeToast("Must enter a location");
            return false;
        }

        if (!FileHandler.checkSessionName(activeFolderName, sessionName, sessionLocation)) {
            makeSomeToast("Session name already exists, will be saved with a version number");
        }

        return true;
    }

    private void setActiveFolder() {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.folder_spinner);

        String folderName = spinner.getSelectedItem().toString();
        activeFolder = new File(FileHandler.getSessionsDirectory(), folderName);
        activeFolderName = activeFolder.getName();
    }

    private boolean loadSelectedTemplate() {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.template_spinner);
        String templateName = spinner.getSelectedItem().toString();

        String template = FileHandler.readTemplate(templateName);

        if (!"".equals(template)) {
            activeTemplate = new Template(template);
            activeTemplateName = templateName;

            observations = new Observation();
            for (int i = 0; i < nObservations; i++) {
                observations.addTemplate(new Template(template));
            }

            return true;
        } else {
            makeSomeToast("Error reading template.");
        }

        return false;
    }

    /**
     * 1. Creates new intent to go to SessionActivity
     * 2. Adds the user defined Session (parcelable so can be putExtra-ed no hassle),
     * 3. Starts SessionActivity.
     */
    public void createSession() {
        db.setFolderTemplate(activeFolderName, activeTemplate.toString());

        Session newSession = new Session(sessionName, sessionLocation, activeFolder.getAbsolutePath());
        newSession.setObservations(observations);
//        newSession.setTemplate(activeTemplate);

        Intent intent = new Intent(getActivity(), SessionActivity.class);
        intent.putExtra("activeFolderString", activeFolderName);
        intent.putExtra("Session", newSession);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent);
    }

    private void makeSomeToast(final String message) {
        final Context context = getActivity();
        final int duration = Toast.LENGTH_SHORT;

        final Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

//    /**
//     * Takes a path to a file and checks if the file is a template.
//     * @param filePath the path to the file to check
//     * @return true if the filePath has the extension .template
//     */
//    public boolean checkTemplateExtension(String filePath) {
//        String[] parts = filePath.split("\\.");
//        if (parts.length == 0) return false;
//        return parts[parts.length - 1].equals(".template");
//    }

//	public void setActiveFolderButton(final View rootView) {
//		Button button = (Button) rootView.findViewById(R.id.select_folder);
//		button.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Spinner spinner = (Spinner) rootView.findViewById(R.id.folder_spinner);
//
//				String folderName = spinner.getSelectedItem().toString();
//				File activeFolder = new File(FileHandler.getSessionsDirectory(), folderName);
//				((HomeActivity) getActivity()).setActiveFolder(activeFolder);
//
//				// Change colour and move to next fragment
//				((HomeActivity) getActivity()).displayFragment(1);
//			}
//
//		});
//	}

}
