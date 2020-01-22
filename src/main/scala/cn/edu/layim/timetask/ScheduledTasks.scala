package cn.edu.layim.timetask

import cn.edu.layim.util.DateUtil
import org.slf4j.{ Logger, LoggerFactory }
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

/**
 * ScheduledTasks定时任务处理
 *
 * 暂未使用
 *
 * @date 2018年9月8日
 * @author 梦境迷离
 */
@Component
@Configurable
@EnableScheduling //启动定时任务
class ScheduledTasks {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[ScheduledTasks])

  /**
   * 每1分钟执行一次
   */
  //@Scheduled(cron = "0 */1 *  * * * ")
  def redisTask = {
    LOGGER.info("Scheduling Tasks Examples By Cron: The time is now " + DateUtil.getDateString)
  }

  //@Scheduled(fixedRate = 1000 * 30)
  def dataBaseTask = {
    LOGGER.info("Scheduling Tasks Examples: The time is now " + DateUtil.getDateString)
  }

}