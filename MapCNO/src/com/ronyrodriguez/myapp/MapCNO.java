package com.ronyrodriguez.myapp;

import com.codename1.components.InfiniteProgress;
import com.codename1.googlemaps.MapContainer;
import com.codename1.googlemaps.MapContainer.MapObject;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.table.TableLayout;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class MapCNO {

    private Form current;
    protected MapObject mapo = new MapObject();
    protected Coord coord = new Coord(0, 0);

    public void init(Object context) {
        try {
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }

        buildForm();
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }

    public void destroy() {

    }

    public void checkGPS(final MapContainer mapc) {
        if (Display.getInstance().getLocationManager().isGPSDetectionSupported()) {
            if (Display.getInstance().getLocationManager().isGPSEnabled()) {
                InfiniteProgress ipr = new InfiniteProgress();
                final Dialog dialog = ipr.showInifiniteBlocking();
                Location loc = LocationManager.getLocationManager().getCurrentLocationSync(10000);
                dialog.dispose();
                if (loc != null) {
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();
                    coord = new Coord(lat, lng);
                    mapc.setCameraPosition(coord);
                    try {
                        mapc.addMarker(EncodedImage.create("/maps-pin.png"), coord, "marker", "", null);
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                } else {
                    Dialog.show("GPS error", "No se pudo encontrar tu ubicación, intenta salir por una mejor señal GPS", "Ok", null);
                }
            } else {
                Dialog.show("GPS Inhabilitado", "Esto necesita acceso al GPS. Habilita el GPS", "Ok", null);
            }
        } else {
            InfiniteProgress ip = new InfiniteProgress();
            final Dialog dialog = ip.showInifiniteBlocking();
            Location loc = LocationManager.getLocationManager().getCurrentLocationSync(10000);
            dialog.dispose();
            if (loc != null) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                coord = new Coord(lat, lng);
                mapc.setCameraPosition(coord);
                try {
                    mapc.addMarker(EncodedImage.create("/maps-pin.png"), coord, "marker", "", null);
                } catch (IOException err) {
                    err.printStackTrace();
                }
            } else {
                Dialog.show("GPS error", "Your location could not be found, please try going outside for a better GPS signal", "Ok", null);
            }
        }
    }

    public void buildForm() {

        final MapContainer mapc = new MapContainer();
        mapc.setTensileDragEnabled(false);
        
        checkGPS(mapc);

        Form form = new Form("MapCNO");
        form.setLayout(new BorderLayout());

        mapc.addTapListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mapc.clearMapLayers();
                try {
                    coord = new Coord(mapc.getCoordAtPosition(evt.getX(), evt.getY()));
                    mapo = mapc.addMarker(EncodedImage.create("/maps-pin.png"), coord, "", "", (evt1) -> {
                        coord = mapc.getCoordAtPosition(evt.getX(), evt.getY());
                    });
                } catch (IOException err) {
                    err.printStackTrace();
                }
                form.revalidate();
            }
        });

        TableLayout tl;
        int spanButton = 2;
        if (Display.getInstance().isTablet()) {
            tl = new TableLayout(7, 2);
        } else {
            tl = new TableLayout(14, 1);
            spanButton = 1;
        }
        tl.setGrowHorizontally(true);
        form.setLayout(tl);

        TextField info = new TextField("", "Información", 20, TextArea.ANY);

        Button submit = new Button("Continuar");

        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (info.getText().toString().equalsIgnoreCase("")) {
                    info.getAllStyles().setBgColor(0xFF0000);
                    info.getComponentForm().repaint();
                } else {
                    info.getAllStyles().setBgColor(0xFFFFFF);
                    info.getComponentForm().repaint();
                    Form f = new Form();
                    Button back = new Button("Regresar");
                    back.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            submit.getComponentForm().showBack();

                        }
                    });

                    TextField tinfo = new TextField(info.getText().toString(), "", 20, TextArea.ANY);
                    tinfo.setEditable(false);
                    tinfo.setEnabled(false);

                    TextField tlat = new TextField(String.valueOf(coord.getLatitude()), "", 20, TextArea.ANY);
                    tlat.setEditable(false);
                    tlat.setEnabled(false);

                    TextField tlong = new TextField(String.valueOf(coord.getLongitude()), "", 20, TextArea.ANY);
                    tlong.setEditable(false);
                    tlong.setEnabled(false);

                    f.add("Latitud:").add(tlat).add("Longitud:").add(tlong).add("Información extra").add(tinfo).add(back);
                    f.show();
                }
            }
        });

        TableLayout.Constraint cn = tl.createConstraint();
        cn.setHorizontalSpan(spanButton);
        cn.setHorizontalAlign(Component.RIGHT);
        form.add("Información").add(info).
                add(cn, submit).add(mapc);

        form.addCommand(new Command("Rony Rodriguez") {
            public void actionPerformed(ActionEvent ev) {
            }
        });

        info.getAllStyles().setBgTransparency(0xFF);
        info.getComponentForm().repaint();

        form.show();
    }

}
