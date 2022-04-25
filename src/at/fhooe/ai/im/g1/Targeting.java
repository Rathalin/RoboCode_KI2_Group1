package at.fhooe.ai.im.g1;

import robocode.util.Utils;

import java.awt.geom.Point2D;

public class Targeting {

    private NewRobot self;
    private double oldEnemyHeading;
    public Targeting(NewRobot self){
        this.self = self;
    }

    public void targetRobot(Enemy target){
        if(target == null || !target.live){
            return;
        }

        SetBearing(target);

        SmartTarget(target);

        self.lastPos = self.myPos;
    }

    private void SetBearing(Enemy target){
        double v2;
        Point2D.Double patPos;
        double v1 = self.myPos.distance(patPos = target.pos);


        self.setTurnGunLeftRadians(Utils.normalRelativeAngle(self.getGunHeadingRadians() - self.calcAngle(patPos, self.myPos)));
    }

    private void CircularTargeting(Enemy target){
        double bulletPower = Math.min(3.0, self.getEnergy());
        double myX = self.getX();
        double myY = self.getY();
        double absoluteBearing = self.getHeadingRadians() + target.bearing;
        double enemyX = myX + target.dist * Math.sin(absoluteBearing);
        double enemyY = myX + target.dist * Math.cos(absoluteBearing);
        double enemyHeading = target.bearing;
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = target.velocity;
        oldEnemyHeading = enemyHeading;

        double deltaTime = 0;
        double battleFieldHeight = self.getBattleFieldHeight(),
                battleFieldWidth = self.getBattleFieldWidth();
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
                predictedX - myX, predictedY - myY));

        self.setTurnRadarRightRadians(Utils.normalRelativeAngle(
                absoluteBearing - self.getRadarHeadingRadians()));
        self.setTurnGunRightRadians(Utils.normalRelativeAngle(
                theta - self.getGunHeadingRadians()));

        SmartTarget(target);
    }

    private void SmartTarget(Enemy target){
        double energy = target.energy;
        double dist = target.dist;
        double selfEnergy = self.getEnergy();
        double minPower = 0.3;
        double firePower = 3;

        if(dist > 50){
            firePower -= 2;
        } else if(dist > 20){
            firePower -= 1;
        }

        if(selfEnergy < 20){
            firePower -= 0.3;
        }

        firePower = Clamp(firePower, minPower, energy);
        self.fire(firePower);
    }

    private double Clamp(double val, double min, double max){
        if(val > max){
            val = max;
        }
        if(val < min){
            val = min;
        }

        return val;
    }
}


