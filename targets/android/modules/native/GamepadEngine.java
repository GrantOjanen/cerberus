
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

class GamepadEngine {

	private static final int CX_JOY_A=0;
	private static final int CX_JOY_B=1;
	private static final int CX_JOY_X=2;
	private static final int CX_JOY_Y=3;
	private static final int CX_JOY_LB=4;
	private static final int CX_JOY_RB=5;
	private static final int CX_JOY_BACK=6;
	private static final int CX_JOY_START=7;
	private static final int CX_JOY_LEFT=8;
	private static final int CX_JOY_UP=9;
	private static final int CX_JOY_RIGHT=10;
	private static final int CX_JOY_DOWN=11;
	private static final int CX_JOY_LSB=12;
	private static final int CX_JOY_RSB=13;
	private static final int CX_JOY_MENU=14;

    private static final int REMAP_NO = 0;
    private static final int REMAP_FIND_OUT = -1;
    private static final int REMAP_YES = 1;
    private int remapPS4_Controllers = REMAP_FIND_OUT;
    private int remapPS5_Controllers = REMAP_FIND_OUT;
    private int remapOUYA_Controllers = REMAP_FIND_OUT;
    private int remapPowerA_Switch_Controllers = REMAP_FIND_OUT;

	int gp_count = 0;
	ArrayList<Gamepad> gamepadsReady = new ArrayList();
	private final ArrayList gamepadsNotReady = new ArrayList();

	class Gamepad {
		boolean[] buttonsPressed = new boolean[]{false,false,false,false,false,false,false,false,
				false,false,false,false,false,false,false,false,false};
		final boolean[] buttonsDown = new boolean[]{false,false,false,false,false,false,false,false,
				false,false,false,false,false,false,false,false,false};
		boolean[] buttonsReleased = new boolean[]{false,false,false,false,false,false,false,false,
				false,false,false,false,false,false,false,false,false};
		float[] axisValues = new float[]{0,0,0,0,0,0};
		boolean connected = false;
		String description = "";
		int deviceID = -1;
		boolean remapAxes = false;
		boolean hasDirectionHat = true;
		boolean usingTriggers = false;
		boolean hasBrakeAndGas = false;
	}

	public void tick() {
		gamepadsReady = new ArrayList();
		int gp_old_count = gamepadsNotReady.size();
		int[] inputs = InputDevice.getDeviceIds();
		for (int input : inputs) {
			InputDevice device = InputDevice.getDevice(input);
			if (((device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
					|| ((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)) {
				if (addGamepadIfNew(device)) {
					gp_old_count = gamepadsNotReady.size();
				}
			}
		}
		for (int i = 0; i < gp_old_count; i ++) {
			Gamepad gp_old = (Gamepad)gamepadsNotReady.get(i);
			InputDevice dev = InputDevice.getDevice(gp_old.deviceID);
			if (dev == null) {
				gp_old.connected = false;
			} else {
				gp_count = i+1;
			}
			Gamepad gp = new Gamepad();
			gp.deviceID = gp_old.deviceID;
			gp.description = gp_old.description;
			gp.connected = gp_old.connected;
			if (gp.connected) {
				gp.axisValues = new float[]{gp_old.axisValues[0],gp_old.axisValues[1],gp_old.axisValues[2],gp_old.axisValues[3],gp_old.axisValues[4],gp_old.axisValues[5]};
			} else {
				gp.axisValues = new float[]{0,0,0,0,0,0};
			}
			System.arraycopy(gp_old.buttonsPressed, 0, gp.buttonsPressed, 0, 17);
			gp_old.buttonsPressed = new boolean[]{false,false,false,false,false,false,false,false,
					false,false,false,false,false,false,false,false,false};
			System.arraycopy(gp_old.buttonsDown, 0, gp.buttonsDown, 0, 17);
			for (int ii = 0; ii < 17; ii ++) {
				gp.buttonsReleased[ii] = gp_old.buttonsReleased[ii];
				if (gp_old.buttonsReleased[ii]) {
					gp.buttonsDown[ii] = false;
					gp_old.buttonsDown[ii] = false;
				}
			}
			gp_old.buttonsReleased = new boolean[]{false,false,false,false,false,false,false,false,
					false,false,false,false,false,false,false,false,false};
			gamepadsReady.add(gp);
		}
		//gp_count = gamepadsReady.size();
	}

	public boolean gamepad_is_connected(int device) {
		if (device >= 0 && gp_count >= device + 1) {
			Gamepad gp = (Gamepad) gamepadsReady.get(device);
			return gp != null && gp.connected;
		}
		return false;
	}

	/*not used in CX
	public int gamepad_get_device_count() {
		return gp_count;
	}

	private static final String empty = "";

	public String gamepad_get_description(int device) {
		if (device >= 0 && gp_count >= device + 1) {
			Gamepad gp = (Gamepad) gamepadsReady.get(device);
			if (gp != null) {
				return gp.description;
			}
		}
		return empty;
	}

	public boolean gamepad_button_check_pressed(int device, int button) {
		if (device >= 0 && gp_count >= device + 1) {
			Gamepad gp = (Gamepad) gamepadsReady.get((int) device);
			return gp != null && gp.connected && gp.buttonsPressed[(int) button];
		}
		return false;
	}

	public boolean gamepad_button_check(int device, int button) {
		if (device >= 0 && gp_count >= device + 1) {
			Gamepad gp = (Gamepad) gamepadsReady.get(device);
			return gp != null && gp.connected && gp.buttonsDown[button];
		}
		return false;
	}

	public boolean gamepad_button_check_released(int device, int button) {
		if (device >= 0 && gp_count >= device + 1) {
			Gamepad gp = (Gamepad) gamepadsReady.get(device);
			return gp != null && gp.connected && gp.buttonsReleased[button];
		}
		return false;
	}

	public float gamepad_axis_value(int device, int axis) {
		if (device >= 0 && gp_count >= device + 1) {
			Gamepad gp = (Gamepad) gamepadsReady.get(device);
			if (gp != null && gp.connected && axis >= 0 && axis <= 3) {
				return gp.axisValues[axis];
			}
		}
		return 0;
	}
	*/

	public boolean onGenericMotion(MotionEvent event) {
		if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_BUTTON_PRESS)) {
			InputDevice device = event.getDevice();
				addGamepadIfNew(device);
			int size = gamepadsNotReady.size();
			for (int i = 0; i < size; i ++) {
				Gamepad gamepad = (Gamepad) gamepadsNotReady.get(i);
				if (gamepad.deviceID == device.getId()) {
					if (gamepad.hasDirectionHat) {
						final float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
						final float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
						axisToButton(hatX,.3f,.02f,CX_JOY_RIGHT,gamepad);
						axisToButton(hatX,-.3f,-.02f,CX_JOY_LEFT,gamepad);
						axisToButton(hatY,.3f,.02f,CX_JOY_DOWN,gamepad);
						axisToButton(hatY,-.3f,-.02f,CX_JOY_UP,gamepad);
					}
					gamepad.axisValues[0] = event.getAxisValue(MotionEvent.AXIS_X);
					gamepad.axisValues[1] = event.getAxisValue(MotionEvent.AXIS_Y);
					float triggerL;
					float triggerR;
					if (!gamepad.remapAxes) {
						gamepad.axisValues[2] = event.getAxisValue(MotionEvent.AXIS_Z);
						gamepad.axisValues[3] = event.getAxisValue(MotionEvent.AXIS_RZ);
						if (!gamepad.hasBrakeAndGas) {
							triggerL = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
							triggerR = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);
						} else {
							triggerL = event.getAxisValue(MotionEvent.AXIS_BRAKE);
							triggerR = event.getAxisValue(MotionEvent.AXIS_GAS);
						}
					} else {
						gamepad.axisValues[2] = event.getAxisValue(MotionEvent.AXIS_RX);
						gamepad.axisValues[3] = event.getAxisValue(MotionEvent.AXIS_RY);
						triggerL = event.getAxisValue(MotionEvent.AXIS_Z);
						triggerR = event.getAxisValue(MotionEvent.AXIS_RZ);
					}
					if (gamepad.usingTriggers || triggerL > 0.05) {
						gamepad.axisValues[4] = triggerL;
						gamepad.usingTriggers = true;
					}
					if (gamepad.usingTriggers || triggerR > 0.05) {
						gamepad.axisValues[5] = triggerR;
						gamepad.usingTriggers = true;
					}
					return true;
				}
			}
		}
		return false;
	}

	private void axisToButton(float axisValue, float threshold1, float threshold2, int buttonIndex, Gamepad gp) {
		if ((threshold1 > 0 && axisValue > threshold1) || (threshold1 < 0 && axisValue < threshold1)) {
			if (!gp.buttonsDown[buttonIndex]) {
				gp.buttonsPressed[buttonIndex] = true;
			}
			gp.buttonsDown[buttonIndex] = true;
		} else if ((threshold2 < 0 && axisValue > threshold2) || (threshold2 > 0 && axisValue < threshold2)) {
			if (gp.buttonsDown[buttonIndex]) {
				gp.buttonsReleased[buttonIndex] = true;
			}
			gp.buttonsDown[buttonIndex] = false;
		}
	}

	private boolean shouldRemapAxes(Gamepad gp, InputDevice device) {
		String name = device.getName();
		List<InputDevice.MotionRange> ranges = device.getMotionRanges();
		boolean triggersFound = false;
		byte joysticksFound = 0;
		int size = ranges.size();
		for (int i = 0; i < size; i++) {
			int iAxis = ranges.get(i).getAxis();
			if (iAxis == MotionEvent.AXIS_BRAKE || iAxis == MotionEvent.AXIS_GAS) {
				gp.hasBrakeAndGas = true;
			}
			if (gp.hasBrakeAndGas || iAxis == MotionEvent.AXIS_RTRIGGER || iAxis == MotionEvent.AXIS_LTRIGGER) {
				triggersFound = true;
			}
			if (iAxis == MotionEvent.AXIS_RX || iAxis == MotionEvent.AXIS_RY) {
				joysticksFound++;
			}
		}
		return !triggersFound && joysticksFound == 2 && (
				name.equals("Microsoft X-Box pad (Japan)")
						|| name.equals("Microsoft X-Box pad v2 (US)")
						|| name.equals("Chinese-made Xbox Controller")
						|| name.equals("Old Xbox pad")
						|| name.equals("Logic3 Controller")
						|| name.equals("Generic X-Box pad"));
	}

	private boolean hasHat(InputDevice device) {
		List<InputDevice.MotionRange> ranges = device.getMotionRanges();
		byte hatAxesFound = 0;
		int size = ranges.size();
		for (int i = 0; i < size; i++) {
			int iAxis = ranges.get(i).getAxis();
			if (iAxis == MotionEvent.AXIS_HAT_X || iAxis == MotionEvent.AXIS_HAT_Y) {
				hatAxesFound++;
			}
		}
		return hatAxesFound >= 2;
	}

	private int remapButtons(int keyCode, KeyEvent event) {
		String name = event.getDevice().getName();
		if (event.getRepeatCount() == 0) {
            if (remapPS4_Controllers != REMAP_NO && Build.VERSION.SDK_INT < 28 && name.equals("Sony Computer Entertainment Wireless Controller")) {//PS4 (always correct on Android 9+)
                if (remapPS4_Controllers == REMAP_FIND_OUT) {
                    if (Arrays.equals(event.getDevice().hasKeys(KeyEvent.KEYCODE_BUTTON_Z, KeyEvent.KEYCODE_BUTTON_C, KeyEvent.KEYCODE_BUTTON_THUMBR, KeyEvent.KEYCODE_DPAD_DOWN), new boolean[]{true, true, false, false}))
                    {remapPS4_Controllers = REMAP_YES;} else {remapPS4_Controllers = REMAP_NO;}
                }
                if (remapPS4_Controllers == REMAP_YES) {
                    switch (keyCode) {
                        case 97: keyCode = KeyEvent.KEYCODE_BUTTON_A;break;
                        case 98: keyCode = KeyEvent.KEYCODE_BUTTON_B;break;
                        case 96: keyCode = KeyEvent.KEYCODE_BUTTON_X;break;
                        case 99: keyCode = KeyEvent.KEYCODE_BUTTON_Y;break;
                        case 105: keyCode = KeyEvent.KEYCODE_BUTTON_START;break;
                        case 104: keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;break;
                        case 100: keyCode = KeyEvent.KEYCODE_BUTTON_L1;break;
                        case 102: keyCode = KeyEvent.KEYCODE_BUTTON_L2;break;
                        case 101: keyCode = KeyEvent.KEYCODE_BUTTON_R1;break;
                        case 103: keyCode = KeyEvent.KEYCODE_BUTTON_R2;break;
                        case 109: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBL;break;
                        case 108: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBR;break;
                    }
                }
            } else if (remapPS5_Controllers != REMAP_NO && name.equals("Sony Interactive Entertainment Wireless Controller")) {//PS5
                if (remapPS5_Controllers == REMAP_FIND_OUT) {
                    if (Arrays.equals(event.getDevice().hasKeys(KeyEvent.KEYCODE_BUTTON_Z, KeyEvent.KEYCODE_BUTTON_C, KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_DPAD_DOWN), new boolean[]{true, true, true, false, false}))
                    {remapPS5_Controllers = REMAP_YES;} else {remapPS5_Controllers = REMAP_NO;}
                }
                if (remapPS5_Controllers == REMAP_YES) {
                    switch (keyCode) {
                        case 97: keyCode = KeyEvent.KEYCODE_BUTTON_A;break;
                        case 98: keyCode = KeyEvent.KEYCODE_BUTTON_B;break;
                        case 96: keyCode = KeyEvent.KEYCODE_BUTTON_X;break;
                        case 99: keyCode = KeyEvent.KEYCODE_BUTTON_Y;break;
                        case 105: keyCode = KeyEvent.KEYCODE_BUTTON_START;break;
                        case 104: keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;break;
                        case 100: keyCode = KeyEvent.KEYCODE_BUTTON_L1;break;
                        case 102: keyCode = KeyEvent.KEYCODE_BUTTON_L2;break;
                        case 101: keyCode = KeyEvent.KEYCODE_BUTTON_R1;break;
                        case 103: keyCode = KeyEvent.KEYCODE_BUTTON_R2;break;
                        case 109: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBL;break;
                        case 108: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBR;break;
                    }
                }
            } else if (remapOUYA_Controllers != REMAP_NO
                    && name.equals("OUYA Game Controller")) {
                if (remapOUYA_Controllers == REMAP_FIND_OUT) {
                    if (Arrays.equals(event.getDevice().hasKeys(KeyEvent.KEYCODE_BUTTON_Z, KeyEvent.KEYCODE_BUTTON_C, KeyEvent.KEYCODE_DPAD_DOWN), new boolean[]{true, true, false}))
                    {remapOUYA_Controllers = REMAP_YES;} else {remapOUYA_Controllers = REMAP_NO;}
                }
                if (remapOUYA_Controllers == REMAP_YES) {
                    switch (keyCode) {
                        //case 96: keyCode = KeyEvent.KEYCODE_BUTTON_A;break;//mapped correctly
                        case 99: keyCode = KeyEvent.KEYCODE_BUTTON_B;break;
                        case 97: keyCode = KeyEvent.KEYCODE_BUTTON_X;break;
                        case 98: keyCode = KeyEvent.KEYCODE_BUTTON_Y;break;
                        case 101: keyCode = KeyEvent.KEYCODE_BUTTON_START;break;//really R1
                        case 100: keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;break;//really L1 (controller doesn't have start/select)
                        //case 100: keyCode = KeyEvent.KEYCODE_BUTTON_L1;break;
                        case 110: keyCode = KeyEvent.KEYCODE_BUTTON_L2;break;
                        //case 101: keyCode = KeyEvent.KEYCODE_BUTTON_R1;break;
                        case 106: keyCode = KeyEvent.KEYCODE_BUTTON_R2;break;
                        case 102: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBL;break;
                        case 103: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBR;break;
                    }
                }
            } else if (remapPowerA_Switch_Controllers != REMAP_NO
                    && name.equals("Core (Plus) Wired Controller")) {
                if (remapPowerA_Switch_Controllers == REMAP_FIND_OUT) {
                    if (Arrays.equals(event.getDevice().hasKeys(KeyEvent.KEYCODE_BUTTON_Z, KeyEvent.KEYCODE_BUTTON_C, KeyEvent.KEYCODE_BUTTON_THUMBR, KeyEvent.KEYCODE_DPAD_DOWN), new boolean[]{true, true, false, false}))
                    {remapPowerA_Switch_Controllers = REMAP_YES;} else {remapPowerA_Switch_Controllers = REMAP_NO;}
                }
                if (remapPowerA_Switch_Controllers == REMAP_YES) {
                    switch (keyCode) {
                        case 98: keyCode = KeyEvent.KEYCODE_BUTTON_A;break;
                        case 99: keyCode = KeyEvent.KEYCODE_BUTTON_X;break;
                        case 97: keyCode = KeyEvent.KEYCODE_BUTTON_B;break;
                        case 96: keyCode = KeyEvent.KEYCODE_BUTTON_Y;break;
                        case 105: keyCode = KeyEvent.KEYCODE_BUTTON_START;break;
                        case 104: keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;break;
                        case 100: keyCode = KeyEvent.KEYCODE_BUTTON_L1;break;
                        case 102: keyCode = KeyEvent.KEYCODE_BUTTON_L2;break;
                        case 101: keyCode = KeyEvent.KEYCODE_BUTTON_R1;break;
                        case 103: keyCode = KeyEvent.KEYCODE_BUTTON_R2;break;
                        case 109: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBL;break;
                        case 108: keyCode = KeyEvent.KEYCODE_BUTTON_THUMBR;break;
                    }
                }
            }
        }
        return keyCode;
	}

	public boolean onKey(int keyCode, KeyEvent event) {
		if (((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
				|| ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
				|| (keyCode >= KeyEvent.KEYCODE_BUTTON_1 && keyCode <= KeyEvent.KEYCODE_BUTTON_16)) {
			keyCode = remapButtons(keyCode, event);
			switch(keyCode) {
				case KeyEvent.KEYCODE_BUTTON_A:
				case KeyEvent.KEYCODE_BUTTON_3:
					buttonAction(event, CX_JOY_A);
					return true;
				case KeyEvent.KEYCODE_BUTTON_B:
				case KeyEvent.KEYCODE_BUTTON_2:
					buttonAction(event, CX_JOY_B);
					return true;
				case KeyEvent.KEYCODE_BUTTON_X:
				case KeyEvent.KEYCODE_BUTTON_4:
					buttonAction(event, CX_JOY_X);
					return true;
				case KeyEvent.KEYCODE_BUTTON_Y:
				case KeyEvent.KEYCODE_BUTTON_1:
					buttonAction(event, CX_JOY_Y);
					return true;
				case KeyEvent.KEYCODE_BUTTON_L1:
				case KeyEvent.KEYCODE_BUTTON_5:
				case KeyEvent.KEYCODE_BUTTON_Z:
					buttonAction(event, CX_JOY_LB);
					return true;
				case KeyEvent.KEYCODE_BUTTON_R1:
				case KeyEvent.KEYCODE_BUTTON_6:
				case KeyEvent.KEYCODE_BUTTON_C:
					buttonAction(event, CX_JOY_RB);
					return true;
				case KeyEvent.KEYCODE_BUTTON_L2:
				case KeyEvent.KEYCODE_BUTTON_7:
					buttonAction(event, 15);
					return true;
				case KeyEvent.KEYCODE_BUTTON_R2:
				case KeyEvent.KEYCODE_BUTTON_8:
					buttonAction(event, 16);
					return true;
				case KeyEvent.KEYCODE_BUTTON_SELECT:
				case KeyEvent.KEYCODE_BUTTON_9:
					buttonAction(event, CX_JOY_BACK);
					return true;
				case KeyEvent.KEYCODE_BUTTON_START:
				case KeyEvent.KEYCODE_BUTTON_10:
				case KeyEvent.KEYCODE_MENU:
					buttonAction(event, CX_JOY_START);
					return true;
				case KeyEvent.KEYCODE_BUTTON_THUMBL:
				case KeyEvent.KEYCODE_BUTTON_11:
					buttonAction(event, CX_JOY_LSB);
					return true;
				case KeyEvent.KEYCODE_BUTTON_THUMBR:
				case KeyEvent.KEYCODE_BUTTON_12:
					buttonAction(event, CX_JOY_RSB);
					return true;
				case KeyEvent.KEYCODE_DPAD_UP:
					buttonAction(event, CX_JOY_UP);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					buttonAction(event, CX_JOY_DOWN);
					return true;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					buttonAction(event, CX_JOY_LEFT);
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					buttonAction(event, CX_JOY_RIGHT);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
					buttonAction(event, CX_JOY_DOWN);
					buttonAction(event, CX_JOY_LEFT);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
					buttonAction(event, CX_JOY_DOWN);
					buttonAction(event, CX_JOY_RIGHT);
					return true;
				case KeyEvent.KEYCODE_DPAD_UP_LEFT:
					buttonAction(event, CX_JOY_UP);
					buttonAction(event, CX_JOY_LEFT);
					return true;
				case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
					buttonAction(event, CX_JOY_UP);
					buttonAction(event, CX_JOY_RIGHT);
					return true;
			}
		}
		return false;
	}

	private void buttonAction(KeyEvent event, int keyCode) {
		InputDevice device = event.getDevice();
		addGamepadIfNew(device);
		int size = gamepadsNotReady.size();
		for (int i = 0; i < size; i ++) {
			Gamepad gamepad = (Gamepad)gamepadsNotReady.get(i);
			if (gamepad.deviceID == device.getId()) {
				final int action = event.getAction();
				if (action == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
					if (keyCode == 15) {
						if (!gamepad.usingTriggers) {
							gamepad.axisValues[4] = 1;
						}
					} else if (keyCode == 16) {
						if (!gamepad.usingTriggers) {
							gamepad.axisValues[5] = 1;
						}
					} else {
						if (gamepad.buttonsReleased[keyCode]) {
							gamepad.buttonsReleased[keyCode] = false;
						}
						if (!gamepad.buttonsPressed[keyCode]) {
							gamepad.buttonsPressed[keyCode] = true;
						}
						if (!gamepad.buttonsDown[keyCode]) {
							gamepad.buttonsDown[keyCode] = true;
						}
					}
				} else if (action == KeyEvent.ACTION_UP) {
					if (keyCode == 15) {
						if (!gamepad.usingTriggers) {
							gamepad.axisValues[4] = 0;
						}
					} else if (keyCode == 16) {
						if (!gamepad.usingTriggers) {
							gamepad.axisValues[5] = 0;
						}
					} else {
						if (gamepad.buttonsDown[keyCode]) {
							gamepad.buttonsDown[keyCode] = false;
						}
						if (gamepad.buttonsPressed[keyCode]) {
							gamepad.buttonsPressed[keyCode] = false;
						}
						if (!gamepad.buttonsReleased[keyCode]) {
							gamepad.buttonsReleased[keyCode] = true;
						}
					}
				}
				break;
			}
		}
	}

	private boolean addGamepadIfNew(InputDevice device) {
		int old_index = -1;
		int size = gamepadsNotReady.size();
		for (int i = 0; i < size; i ++) {
			Gamepad old_gp = (Gamepad)gamepadsNotReady.get(i);
			if (!old_gp.connected) {
				old_index = i;
			}
			if (old_gp.deviceID == device.getId()) {
				return false;
			}
		}
		Gamepad gp = new Gamepad();
		gp.deviceID = device.getId();
		gp.description = device.getName();
		gp.connected = true;
		gp.remapAxes = shouldRemapAxes(gp, device);
		gp.hasDirectionHat = hasHat(device);
		if (old_index != -1) {
			gamepadsNotReady.remove(old_index);
			gamepadsNotReady.add(old_index, gp);
		} else {
			gamepadsNotReady.add(gp);
		}
		return true;
	}
}