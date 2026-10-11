package org.acme;


import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import dev.langchain4j.service.decision.Choice;
import dev.langchain4j.service.decision.Decide;
import dev.langchain4j.service.decision.DecisionServices;
import dev.langchain4j.service.decision.Scale;

public class MainApp {

    enum Team {
        @Description("Payments, payouts, invoices, refunds") BILLING,
        @Description("Problems using the product") SUPPORT,
        @Description("Pricing, upgrades, new accounts") SALES
    }

    enum Severity {
        @Description("Cosmetic issue, no impact") LOW,
        @Description("A feature is degraded, a workaround exists") MEDIUM,
        @Description("A feature is broken for some customers") HIGH,
        @Description("Outage or data loss") CRITICAL
    }

    interface SupportDesk {

        @Decide("Is this message spam?")
        boolean isSpam(String message);

        @Decide("Is this message spam?")
        YesNoAnswer spamProbability(String message);

        @Decide("Which team should handle this ticket?")
        Team route(String ticket);

        @Decide("Which team should handle this ticket?")
        Choice<Team> routeWithProbabilities(String ticket);

        @Decide("How severe is this incident?")
        Scale<Severity> severity(String incident);
    }

    public static void main(String[] args) {

        DecisionModel decisionModel = TypeSafeDecisionModel.builder()
            .modelName("laya-rl-agent")
            .baseUrl("http://localhost:8100")
            .logRequests(true)
            .logResponses(true)
            .build();

        SupportDesk supportDesk = DecisionServices.create(SupportDesk.class, decisionModel);

        System.out.println("Spam boolean");

        System.out.println(supportDesk.isSpam("Congratulations! You won a free cruise!"));

        System.out.println("*".repeat(80));

        System.out.println("Spam Yes No");

        YesNoAnswer yesNoAnswer = supportDesk.spamProbability("Congratulations! You won a free cruise!");

        System.out.println(yesNoAnswer.isYes(0.6));
        System.out.println(yesNoAnswer.probability());

        System.out.println("*".repeat(80));

        System.out.println("Routing");

        Team team = supportDesk.route("I was charged twice this month");

        System.out.println(team);

        System.out.println("*".repeat(80));

        System.out.println("Routing Choice");

        Choice<Team> teamChoice = supportDesk.routeWithProbabilities("I was charged twice this month");

        System.out.println(teamChoice.value());
        System.out.println(teamChoice.probabilities());

        System.out.println("*".repeat(80));

        System.out.println("Severity");

        Scale<Severity> severity = supportDesk
            .severity("The button of the webpage is green instead of light green");

        System.out.println(severity.mostLikely());
        System.out.println(severity.probabilities());

    }
}
