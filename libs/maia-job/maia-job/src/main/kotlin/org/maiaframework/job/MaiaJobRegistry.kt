package org.maiaframework.job

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class MaiaJobRegistry: ApplicationContextAware, InitializingBean {


    private lateinit var applicationContext: ApplicationContext

    private val jobsByName = mutableMapOf<JobName, MaiaJob>()


    override fun setApplicationContext(applicationContext: ApplicationContext) {

        this.applicationContext = applicationContext

    }


    override fun afterPropertiesSet() {

        this.applicationContext.getBeansOfType(MaiaJob::class.java).forEach { (beanName, bean) ->

            val existingJob = this.jobsByName.put(bean.jobName, bean)

            if (existingJob != null) {
                throw RuntimeException("Duplicate job: jobName='${bean.jobName}', springBeanName='$beanName', type='${bean.javaClass.name}'")
            }

        }

    }


    fun getJob(jobName: JobName): MaiaJob {

        return jobsByName[jobName]
                ?: throw RuntimeException("No job is registered with name '$jobName'")

    }


    fun getAllJobNames(): List<JobName> {

        return this.jobsByName.keys.toList()

    }


    fun getAllJobDescriptions(): List<JobDescription> {

        return this.jobsByName.map { JobDescription(it.key, it.value.description) }

    }


}
