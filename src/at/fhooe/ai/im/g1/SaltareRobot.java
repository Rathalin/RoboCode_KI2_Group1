package at.fhooe.ai.im.g1;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;

public class SaltareRobot extends AdvancedRobot {

    //    private double lastHitBulletSpeed = 15D;
    private double moveDirection = 1D;

    @Override
    public void run() {
        setBodyColor(Color.blue);

        turnRadarRightRadians(Double.POSITIVE_INFINITY);
        while (true) {
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
        setTurnRight(e.getBearing() + 90D);
        setAhead(100 * moveDirection);
        if (randomChance(5)) {
            switchMoveDirection();
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        switchMoveDirection();
    }


    private boolean randomChance(double percent) {
        return Math.random() * 100D < percent;
    }


    private void switchMoveDirection() {
        moveDirection *= -1.0;
    }

}
