package org.maiaframework.showcase.toggles

import org.maiaframework.toggles.Feature
import org.maiaframework.toggles.FeatureToggleProvider
import org.maiaframework.toggles.fields.ContactPerson
import org.maiaframework.toggles.fields.Description
import org.maiaframework.toggles.fields.InfoLink
import org.maiaframework.toggles.fields.TicketKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate


object ShowcaseNewDashboard : Feature(
    contactPerson = ContactPerson("Muriel"),
    description = Description("Redesigned landing dashboard"),
    ticketKey = TicketKey("SHOW-101"),
    infoLink = InfoLink("https://example.com/tickets/SHOW-101"),
    reviewDate = LocalDate.of(2099, 1, 1),
    enabledByDefault = true,
)


object ShowcaseBetaExport : Feature(
    contactPerson = ContactPerson("Kathleen"),
    description = Description("Beta CSV export of blotter data"),
    ticketKey = TicketKey("SHOW-202"),
    reviewDate = LocalDate.of(2020, 1, 1),
)


object ShowcaseDarkLaunch : Feature()


@Configuration
class ShowcaseFeaturesConfiguration {


    @Bean
    fun showcaseFeatureToggleProvider(): FeatureToggleProvider {

        return FeatureToggleProvider(
            ShowcaseNewDashboard,
            ShowcaseBetaExport,
            ShowcaseDarkLaunch,
        )

    }


}
