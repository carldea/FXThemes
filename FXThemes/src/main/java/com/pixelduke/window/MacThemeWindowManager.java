package com.pixelduke.window;

import com.sun.jna.*;
import com.sun.jna.platform.mac.CoreFoundation;
import javafx.stage.Window;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.pixelduke.window.FoundationLibrary.getNativeHandleOfStage;

public class MacThemeWindowManager implements ThemeWindowManager {
    long NSUTF16LittleEndianStringEncoding = 0x94000100;
    static Pointer initWithBytesLengthEncodingSel = FoundationLibrary.INSTANCE.sel_registerName("initWithBytes:length:encoding:");
    @Override
    public void setDarkModeForWindowFrame(Window window, boolean darkMode) {

        Pointer nsWindowPtr = getNativeHandleOfStage(window);
        System.out.println("Native NSWindow ptr as string: " + nsWindowPtr);

        System.out.println("testing darkMode called: " + darkMode);

        /*
        The general flow of to blur the javafx NSView (subviews[0]). HostView is the contentView (NSView).

  (done)    NSVisualEffectView *blurView = [[NSVisualEffectView alloc] initWithFrame:[self.window.contentView bounds]];
  (done)    [blurView setAppearance:[NSAppearance appearanceNamed:NSAppearanceNameVibrantDark]];
  (done?)   [blurView setBlendingMode:NSVisualEffectBlendingModeBehindWindow];
  (done)    [blurView setMaterial:NSVisualEffectMaterialUnderWindowBackground]; // 21
  (done)    [blurView setState:NSVisualEffectStateActive];
  ([replaced by the following])    [self.window.contentView addSubview:blurView];
  (done)    [vfxView setAutoresizingMask: (NSViewWidthSizable|NSViewHeightSizable)]; 2 | 16
  (done)    [hostView addSubview: vfxView positioned: NSWindowBelow relativeTo: jfxView];

         */

        // get content view (NSView ptr)
        Pointer contentViewSelector = AppKitLibrary.INSTANCE.sel_registerName("contentView");
        Pointer contentViewPtr = AppKitLibrary.INSTANCE.objc_msgSend(nsWindowPtr, contentViewSelector);

        // get bounds (NSRect from NSView.bounds)
        Pointer boundsSelector = AppKitLibrary.INSTANCE.sel_registerName("bounds");
        Pointer nsRectObj = AppKitLibrary.INSTANCE.objc_msgSend(contentViewPtr, boundsSelector);

        // create space for NSVisualEffectView obj
        Pointer nsVisEffectViewClass = AppKitLibrary.INSTANCE.objc_getClass("NSVisualEffectView");
        Pointer allocSel = AppKitLibrary.INSTANCE.sel_registerName("alloc");
        Pointer nsVisEffectViewPtr = AppKitLibrary.INSTANCE.objc_msgSend(nsVisEffectViewClass, allocSel);

        // create a pointer and construct a NSVisualEffectView via initWithFrame.
        Pointer initWithFrameSel = AppKitLibrary.INSTANCE.sel_registerName("initWithFrame:");
        Pointer blurViewPtr = AppKitLibrary.INSTANCE.objc_msgSend(nsVisEffectViewPtr, initWithFrameSel, nsRectObj);

        // [blurView setAppearance:[NSAppearance appearanceNamed:NSAppearanceNameVibrantDark]];
        Pointer nsNameVibrantDarkPtr = AppKitLibrary.getGlobalVariableRef("NSAppearanceNameVibrantDark");
        Pointer nsAppearanceName = AppKitLibrary.INSTANCE.objc_getClass("NSAppearanceName");

        System.out.println("1 NSAppearanceNameVibrantDark pointer is: " + nsNameVibrantDarkPtr.toString());
        System.out.println("2 NSAppearanceNameVibrantDark string  is: " + FoundationLibrary.toNativeString(nsNameVibrantDarkPtr.getNativeLong(0l)));

        Pointer nsAppearanceClass = AppKitLibrary.INSTANCE.objc_getClass("NSAppearance");

        System.out.println("3 NSAppearance class is: " + nsAppearanceClass);

        Pointer nsAppearanceClassName = AppKitLibrary.INSTANCE.class_getName(nsAppearanceClass);
        System.out.println("before 3.5"); // seems to work by showing a C string of the Class' name.
        System.out.println("3.5 NSAppearance class from address: " + nsAppearanceClassName.getString(0));

        // [NSAppearance appearanceNamed:NSAppearanceNameVibrantDark]
        NativeLong nsAppearanceNameVibrantDark = FoundationLibrary.fromJavaString("NSAppearanceNameVibrantDark");
        System.out.println("4 nsAppearanceNameVibrantDark string  is: " + FoundationLibrary.toNativeString(nsAppearanceNameVibrantDark));

        // I have know idea why I cannot create an NSAppearance to be set. It crashes invoking a Class method.
//        Pointer appearanceNamedSel = AppKitLibrary.INSTANCE.sel_registerName("appearanceNamed:");
//        System.out.println("5. appearanceNamedSel " + appearanceNamedSel);
//        Pointer appearance = AppKitLibrary.INSTANCE.objc_msgSend(nsAppearanceClass, appearanceNamedSel, new Pointer(nsAppearanceNameVibrantDark.longValue()));

//        [blurView setBlendingMode:NSVisualEffectBlendingModeBehindWindow]; // Unsure if a 1 long is the correct enum
        Pointer nsNSVisualEffectBlendingModePtr = AppKitLibrary.INSTANCE.objc_getClass("NSVisualEffectBlendingMode");
        System.out.println("6. nsNSVisualEffectBlendingModePtr is " + nsNSVisualEffectBlendingModePtr);

        Pointer setBlendingModeSel = AppKitLibrary.INSTANCE.sel_registerName("setBlendingMode:");
        AppKitLibrary.INSTANCE.objc_msgSend(blurViewPtr, setBlendingModeSel, 1l); // ??
        System.out.println("7. setBlendingModeSel is " + setBlendingModeSel);

//        [blurView setMaterial:NSVisualEffectMaterialUnderWindowBackground]; // 21
        Pointer setMaterialSel = AppKitLibrary.INSTANCE.sel_registerName("setMaterial:");
        AppKitLibrary.INSTANCE.objc_msgSend(blurViewPtr, setMaterialSel, 21l);
        System.out.println("8. setMaterialSel is " + setMaterialSel);

//        [blurView setState:NSVisualEffectStateActive]; // Not sure if this is the correct enum long value.
        Pointer setStateSel = AppKitLibrary.INSTANCE.sel_registerName("setState:");
        AppKitLibrary.INSTANCE.objc_msgSend(blurViewPtr, setStateSel, 1l); // ??
        System.out.println("9. setStateSel is " + setMaterialSel);

//        NSView* jfxView = hostView.subviews[0]; // objectAtIndex: didn't work so I used (firstObject) instead.
        Pointer subviewsSel = AppKitLibrary.INSTANCE.sel_registerName("subviews");
        System.out.println("10. subviewsSel is " + subviewsSel);
        Pointer subViewsArray = AppKitLibrary.INSTANCE.objc_msgSend(contentViewPtr, subviewsSel);
        System.out.println("11. subViewsArray is " + subViewsArray);

        Pointer objectAtIndexSel = AppKitLibrary.INSTANCE.sel_registerName("firstObject");
        Pointer jfxView = AppKitLibrary.INSTANCE.objc_msgSend(subViewsArray, objectAtIndexSel);
        System.out.println("12. jfxView is " + jfxView);

//        [vfxView setAutoresizingMask: (NSViewWidthSizable|NSViewHeightSizable)]; 2 | 16
        Pointer setAutoresizingMaskSel = AppKitLibrary.INSTANCE.sel_registerName("setAutoresizingMask:");
        AppKitLibrary.INSTANCE.objc_msgSend(jfxView, setAutoresizingMaskSel, new NativeLong(2l | 16l));
        System.out.println("13. setAutoresizingMaskSel is " + setAutoresizingMaskSel);

//        [hostView addSubview: vfxView positioned: NSWindowBelow relativeTo: jfxView];
        Pointer addSubviewPosRelToSel = AppKitLibrary.INSTANCE.sel_registerName("addSubview:positioned:relativeTo:");
        AppKitLibrary.INSTANCE.objc_msgSend(contentViewPtr, addSubviewPosRelToSel, blurViewPtr, new NativeLong(-1l), jfxView);
        System.out.println("14. addSubviewPosRelToSel is " + addSubviewPosRelToSel);

    }
}
// The interface of the runtime library of Objective-C.
interface FoundationLibrary extends Library {
    NativeLong NULL = new NativeLong(0l);
    FoundationLibrary INSTANCE = Native.load(
            "Foundation",
            FoundationLibrary.class,
            Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

    // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
    NativeLong objc_getClass(String className);

    // https://developer.apple.com/documentation/objectivec/1418760-objc_lookupclass?language=objc
    NativeLong objc_lookUpClass(String className);

    // https://developer.apple.com/documentation/objectivec/1418571-sel_getname?language=objc


    // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
    Pointer sel_registerName(String selectorName);

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    // The return type is actually "generic". You might need to declare this function
    // multiple times with different return types if you need them.
    //void objc_msgSend(Pointer receiver, Pointer selector, Object... args);

    NativeLong objc_msgSend(NativeLong receiver, Pointer selector);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, Pointer ...obj);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, NativeLong ...objAddress);
    // Used by NSString.fromJavaString
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, byte[] bytes, int len, long encoding);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, boolean boolArg);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, double floatArg);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, Double red, Double green, Double blue, Double alpha);

    NativeLong stringCls = FoundationLibrary.INSTANCE.objc_getClass("NSString");
    Pointer stringSel = FoundationLibrary.INSTANCE.sel_registerName("string");
    Pointer allocSel = FoundationLibrary.INSTANCE.sel_registerName("alloc");
    Pointer initWithBytesLengthEncodingSel = FoundationLibrary.INSTANCE.sel_registerName("initWithBytes:length:encoding:");
    long NSUTF16LittleEndianStringEncoding = 0x94000100;

    static String toNativeString(NativeLong nativeLong) {
        if (NULL.equals(nativeLong)) {
            return null;
        }
        CoreFoundation.CFStringRef cfString = new CoreFoundation.CFStringRef(new Pointer(nativeLong.longValue()));
        try {
            return CoreFoundation.INSTANCE.CFStringGetLength(cfString).intValue() > 0 ? cfString.stringValue() : "";
        } finally {
            cfString.release();
        }
    }
    static NativeLong fromJavaString(String s) {
        if (s.isEmpty()) {
            return FoundationLibrary.INSTANCE.objc_msgSend(stringCls, stringSel);
        }

        byte[] utf16Bytes = s.getBytes(Charset.forName("UTF-16LE"));
        return FoundationLibrary.INSTANCE.objc_msgSend(FoundationLibrary.INSTANCE.objc_msgSend(stringCls, allocSel),
                initWithBytesLengthEncodingSel, utf16Bytes, utf16Bytes.length, NSUTF16LittleEndianStringEncoding);
    }
    static Pointer getNativeHandleOfStage(Window stage) {
        try {
            final Method getPeer = Window.class.getDeclaredMethod("getPeer");
            getPeer.setAccessible(true);
            final Object tkStage = getPeer.invoke(stage);
            final Method getRawHandle = tkStage.getClass().getMethod("getRawHandle");
            getRawHandle.setAccessible(true);
            Pointer ptr = new Pointer((Long) getRawHandle.invoke(tkStage));
            return ptr;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

class MyCFloatStruct extends Structure {
    public float color;
}


interface AppKitLibrary extends Library {
    NativeLong NULL = new NativeLong(0l);
    AppKitLibrary INSTANCE = Native.load(
            "AppKit",
            AppKitLibrary.class,
            Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

    // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
    Pointer objc_getClass(String className);

    // https://developer.apple.com/documentation/objectivec/1418760-objc_lookupclass?language=objc
    Pointer objc_lookUpClass(String className);

    // https://developer.apple.com/documentation/objectivec/1418571-sel_getname?language=objc

    Pointer class_getName(Pointer cls);
    Pointer object_getClassName(Pointer classPtr);

    // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
    Pointer sel_registerName(String selectorName);

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    // The return type is actually "generic". You might need to declare this function
    // multiple times with different return types if you need them.
    Pointer objc_msgSend(Pointer receiver, Pointer selector, Object... args);
    //Pointer objc_msgSend(Pointer receiver, Pointer selector, Object... args);

    NativeLong objc_msgSend(NativeLong receiver, Pointer selector);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, Pointer ...obj);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, NativeLong ...objAddress);
    // Used by NSString.fromJavaString
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, byte[] bytes, int len, long encoding);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, boolean boolArg);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, double floatArg);
    NativeLong objc_msgSend(NativeLong receiver, Pointer selector, Double red, Double green, Double blue, Double alpha);

//    NativeLong stringCls = FoundationLibrary.INSTANCE.objc_getClass("NSString");
//    Pointer stringSel = FoundationLibrary.INSTANCE.sel_registerName("string");
//    Pointer allocSel = FoundationLibrary.INSTANCE.sel_registerName("alloc");
//    Pointer initWithBytesLengthEncodingSel = FoundationLibrary.INSTANCE.sel_registerName("initWithBytes:length:encoding:");
//    long NSUTF16LittleEndianStringEncoding = 0x94000100;
//
//    static String toNativeString(NativeLong nativeLong) {
//        if (NULL.equals(nativeLong)) {
//            return null;
//        }
//        CoreFoundation.CFStringRef cfString = new CoreFoundation.CFStringRef(new Pointer(nativeLong.longValue()));
//        try {
//            return CoreFoundation.INSTANCE.CFStringGetLength(cfString).intValue() > 0 ? cfString.stringValue() : "";
//        } finally {
//            cfString.release();
//        }
//    }
//    static NativeLong fromJavaString(String s) {
//        if (s.isEmpty()) {
//            return FoundationLibrary.INSTANCE.objc_msgSend(stringCls, stringSel);
//        }
//
//        byte[] utf16Bytes = s.getBytes(Charset.forName("UTF-16LE"));
//        return FoundationLibrary.INSTANCE.objc_msgSend(FoundationLibrary.INSTANCE.objc_msgSend(stringCls, allocSel),
//                initWithBytesLengthEncodingSel, utf16Bytes, utf16Bytes.length, NSUTF16LittleEndianStringEncoding);
//    }
    static Pointer getGlobalVariableRef(String globalVariableName) {
        Pointer globalAddress = NativeLibrary.getInstance("AppKit").getGlobalVariableAddress(globalVariableName);

        return globalAddress;
    }
    static NativeLong getNativeHandleOfStage(Window stage) {
        try {
            final Method getPeer = Window.class.getDeclaredMethod("getPeer");
            getPeer.setAccessible(true);
            final Object tkStage = getPeer.invoke(stage);
            final Method getRawHandle = tkStage.getClass().getMethod("getRawHandle");
            getRawHandle.setAccessible(true);
            NativeLong nativeLong = new NativeLong((Long) getRawHandle.invoke(tkStage));
            return nativeLong;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}