package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import ru.otus.hw.domain.Acquaintance;
import ru.otus.hw.domain.BirthRequest;
import ru.otus.hw.domain.Education;
import ru.otus.hw.domain.FamilyPlan;
import ru.otus.hw.domain.Person;
import ru.otus.hw.service.BirthService;
import ru.otus.hw.service.CareerService;
import ru.otus.hw.service.ChildhoodService;
import ru.otus.hw.service.EducationService;
import ru.otus.hw.service.FamilyService;
import ru.otus.hw.service.LoveService;
import ru.otus.hw.service.SocialService;
import ru.otus.hw.service.StageLogger;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@IntegrationComponentScan(basePackages = "ru.otus.hw.gateway")
public class LifeFlowConfig {

    private final BirthService birthService;

    private final ChildhoodService childhoodService;

    private final EducationService educationService;

    private final CareerService careerService;

    private final SocialService socialService;

    private final LoveService loveService;

    private final FamilyService familyService;

    private final StageLogger stageLogger;

    @Bean
    public IntegrationFlow lifeFlow() {
        return IntegrationFlow.from(Channels.LIFE)
                .transform(BirthRequest.class, birthService::bear)
                .wireTap(Channels.STAGE_LOG)
                .handle(Person.class, (person, headers) -> childhoodService.grow(person))
                .wireTap(Channels.STAGE_LOG)
                .gateway(educationFlow())
                .wireTap(Channels.STAGE_LOG)
                .handle(Person.class, (person, headers) -> careerService.startWorking(person))
                .wireTap(Channels.STAGE_LOG)
                .gateway(friendsFlow())
                .wireTap(Channels.STAGE_LOG)
                .handle(Person.class, (person, headers) -> loveService.seekLove(person))
                .wireTap(Channels.STAGE_LOG)
                .route(Person.class, Person::isMarried, router -> router
                        .subFlowMapping(true, married -> married.gateway(familyFlow()))
                        .subFlowMapping(false, single -> single
                                .handle(Person.class, (person, headers) -> familyService.liveAlone(person))))
                .wireTap(Channels.STAGE_LOG)
                .bridge()
                .get();
    }

    @Bean
    public IntegrationFlow educationFlow() {
        return flow -> flow
                .handle(Person.class, (person, headers) -> educationService.enroll(person))
                .route(Person.class, Person::getEducation, router -> router
                        .subFlowMapping(Education.SCHOOL, school -> school
                                .handle(Person.class, (person, headers) -> educationService.finishSchool(person)))
                        .subFlowMapping(Education.COLLEGE, college -> college
                                .handle(Person.class, (person, headers) -> educationService.studyAtCollege(person)))
                        .subFlowMapping(Education.UNIVERSITY, university -> university
                                .handle(Person.class,
                                        (person, headers) -> educationService.studyAtUniversity(person))));
    }

    @Bean
    public IntegrationFlow friendsFlow() {
        return flow -> flow
                .split(Person.class, socialService::meetPeople)
                .transform(Acquaintance.class, socialService::getAcquainted)
                .aggregate()
                .transform(List.class, socialService::makeFriends);
    }

    @Bean
    public IntegrationFlow familyFlow() {
        return flow -> flow
                .transform(Person.class, familyService::planChildren)
                .route(FamilyPlan.class, FamilyPlan::hasChildren, router -> router
                        .subFlowMapping(true, parenthood -> parenthood
                                .split(FamilyPlan.class, familyService::conceive)
                                .gateway(Channels.LIFE)
                                .aggregate()
                                .transform(List.class, familyService::raise))
                        .subFlowMapping(false, childless -> childless
                                .transform(FamilyPlan.class, familyService::stayChildless)));
    }

    @Bean
    public IntegrationFlow stageLogFlow() {
        return IntegrationFlow.from(Channels.STAGE_LOG)
                .handle(message -> stageLogger.log((Person) message.getPayload()))
                .get();
    }
}
