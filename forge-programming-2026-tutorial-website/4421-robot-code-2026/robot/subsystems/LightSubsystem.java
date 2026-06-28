// package frc.robot.subsystems;

// import com.ctre.phoenix6.configs.CANdiConfigurator;
// import com.ctre.phoenix6.configs.CANdleConfiguration;
// import com.ctre.phoenix6.configs.CANdleConfigurator;
// import com.ctre.phoenix6.controls.ColorFlowAnimation;
// import com.ctre.phoenix6.controls.FireAnimation;
// import com.ctre.phoenix6.controls.LarsonAnimation;
// import com.ctre.phoenix6.controls.RainbowAnimation;
// import com.ctre.phoenix6.controls.RgbFadeAnimation;
// import com.ctre.phoenix6.controls.SolidColor;
// import com.ctre.phoenix6.hardware.CANdle;
// import com.ctre.phoenix6.signals.LarsonBounceValue;
// import com.ctre.phoenix6.signals.RGBWColor;

// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.wpilibj2.command.SubsystemBase;

// public class LightSubsystem extends SubsystemBase{
//     private CANdle candle;
//     private RainbowAnimation rgbAnimation;
//     private LarsonAnimation pipeAnimation;
//     private SolidColor black;
//     private boolean llTwoUpdate = false;
//     private boolean llOneUpdate = false;

//     public LightSubsystem(){
//         candle = new CANdle(62, "canivore");

//         CANdleConfigurator ledConfig = candle.getConfigurator();
//         CANdleConfiguration candleConfiguration = new CANdleConfiguration();

//         candleConfiguration.LED.BrightnessScalar = 1.0;

//         ledConfig.apply(candleConfiguration);

//         rgbAnimation = new RainbowAnimation(76, 243).withSlot(0).withFrameRate(30);
//         pipeAnimation = new LarsonAnimation(276, 309).withSlot(1).withColor(new RGBWColor(255, 0, 0)).withSize(10).withFrameRate(45);

        
//         black = new SolidColor(244, 275).withColor(new RGBWColor(0, 0, 0, 0));

//         candle.setControl(rgbAnimation);
//         candle.setControl(black);
//         candle.setControl(pipeAnimation);
        

//     }

//     @Override
//     public void periodic() {
//         llTwoUpdate = SmartDashboard.getBoolean("LL TWO UPDATE", false);
//         llOneUpdate = SmartDashboard.getBoolean("LL ONE UPDATE", false);

//         pipeAnimation = new LarsonAnimation(276, 309).withSlot(1).withColor(new RGBWColor(llTwoUpdate ? 0 : 255, llTwoUpdate ? 255 :0, 0)).withSize(10).withFrameRate(llOneUpdate ? 100 : 45);
        
//         candle.setControl(pipeAnimation);
        
//     }
// }
