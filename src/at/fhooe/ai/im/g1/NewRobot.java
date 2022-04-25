package at.fhooe.ai.im.g1;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class NewRobot extends AdvancedRobot {

    private Enemy target = new Enemy();
    private Targeting targeting;
    public Point2D.Double myPos;
    public Point2D.Double lastPos;
    public Point2D.Double next;
    static double maxX;
    static double maxY;

    @Override
    public void run() {
//        ahead(50);
//        turnLeft(180);
//        back(50);
        maxX = this.getBattleFieldWidth();
        maxY = this.getBattleFieldHeight();
        targeting = new Targeting(this);

        while (true) {

            myPos = point(getX(), getY());
            turnGunRight(10);
//
//            if (target != null && target.live) {
//                targeting.targetRobot(target);
//            } else {
//                next = lastPos = myPos;
//            }
        }
    }
    private double oldEnemyHeading;
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
//        Enemy en = new Enemy();
//        en.name = event.getName();
//        en.pos = calcPoint(myPos, event.getDistance(), this.getHeadingRadians() + event.getBearingRadians());
//        en.energy = event.getEnergy();
//        en.live = true;
//        en.headingRadians = event.getHeadingRadians();
//        en.velocity = event.getVelocity();
//        target = en;
//
//        double absoluteBearing = getHeading() + event.getBearing();
//        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
//
//        // If it's close enough, fire!
//        if (Math.abs(bearingFromGun) <= 3) {
//            turnGunRight(bearingFromGun);
//            // We check gun heat here, because calling fire()
//            // uses a turn, which could cause us to lose track
//            // of the other robot.
//            if (getGunHeat() == 0) {
//                fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
//            }
//        } // otherwise just set the gun to turn.
//        // Note:  This will have no effect until we call scan()
//        else {
//            turnGunRight(bearingFromGun);
//        }
//
//        if (bearingFromGun == 0) {
//            scan();
//        }

        double bulletPower = Math.min(3.0,getEnergy());
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = e.getVelocity();
        oldEnemyHeading = enemyHeading;

        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight(),
                battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX, predictedY = enemyY;
        while((++deltaTime) * (20.0 - 3.0 * bulletPower) <
                Point2D.Double.distance(myX, myY, predictedX, predictedY)){
            predictedX += Math.sin(enemyHeading) * enemyVelocity;
            predictedY += Math.cos(enemyHeading) * enemyVelocity;
            enemyHeading += enemyHeadingChange;
            if(	predictedX < 18.0
                    || predictedY < 18.0
                    || predictedX > battleFieldWidth - 18.0
                    || predictedY > battleFieldHeight - 18.0){

                predictedX = Math.min(Math.max(18.0, predictedX),
                        battleFieldWidth - 18.0);
                predictedY = Math.min(Math.max(18.0, predictedY),
                        battleFieldHeight - 18.0);
                break;
            }
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(
                predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(Utils.normalRelativeAngle(
                absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(
                theta - getGunHeadingRadians()));
        fire(3);
    }

    public Point2D.Double point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    public Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
        return point(p.x + dist * Math.sin(ang), p.y + dist * Math.cos(ang));
    }

    public double calcAngle(Point2D.Double p2, Point2D.Double p1) {
        return Math.atan2(p2.x - p1.x, p2.y - p1.y);
    }

    public double distToWall(Point2D.Double p) {
        return Math.min(Math.min(p.x, maxX - p.x), Math.min(p.y, maxY - p.y));
    }
}
