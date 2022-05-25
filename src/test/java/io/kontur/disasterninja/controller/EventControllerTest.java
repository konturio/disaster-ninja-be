package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.service.EventApiService;
import io.kontur.disasterninja.service.UserProfileService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventControllerTest {

    private final EventApiService eventApiService = mock(EventApiService.class);
    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private EventFeedDto publicFeed1;
    private EventFeedDto publicFeed2;
    private EventsController eventsController;

    @BeforeEach
    public void before() {
        publicFeed1 = feed(1);
        publicFeed2 = feed(2);

        eventsController = new EventsController(eventApiService, userProfileService);
    }

    @Test
    public void noDefaultFeedReturnedByUps_singleFeedReturnedByEventApiTest() {
        givenUpsReturnsNoDefaultFeed();
        givenEventApiReturnsOnlyFeed2();

        List<EventFeedDto> result = getUserFeedsFromService();

        thenThereIsJustOneFeedInTheResultAndItIsDefault(result);
    }

    @Test
    public void noDefaultFeedReturnedByUps_twoFeedsReturnedByEventApiTest() {
        givenUpsReturnsNoDefaultFeed();
        givenEventApiReturnsTwoFeeds();

        List<EventFeedDto> result = getUserFeedsFromService();

        assertEquals(2, result.size());
        //ups returns nothing - so we mark the first of the feeds returned by event api default
        assertDto1(true, result.get(0));
        assertDto2(false, result.get(1));
    }

    @Test
    public void fefaultFeedReturnedByUps_singleFeedReturnedByEventApiTest() {
        givenUpsReturnsPublicFeed2AsDefault();
        givenEventApiReturnsOnlyFeed2();

        List<EventFeedDto> result = getUserFeedsFromService();

        thenThereIsJustOneFeedInTheResultAndItIsDefault(result);
    }

    @Test
    public void defaultFeedReturnedByUps_twoFeedsReturnedByEventApiTest() {
        givenUpsReturnsPublicFeed2AsDefault();
        givenEventApiReturnsTwoFeeds();

        List<EventFeedDto> result = getUserFeedsFromService();

        assertEquals(2, result.size());
        assertDto1(false, result.get(0));
        //ups says that the default feed is feed2
        assertDto2(true, result.get(1));
    }

    private void thenThereIsJustOneFeedInTheResultAndItIsDefault(List<EventFeedDto> result) {
        assertEquals(1, result.size());
        assertDto2(true, result.get(0)); //just one feed returned by event api so it has to be default
    }

    private List<EventFeedDto> getUserFeedsFromService() {
        return eventsController.getUserFeeds();
    }

    private void givenEventApiReturnsTwoFeeds() {
        when(eventApiService.getUserFeeds())
                .thenReturn(Lists.newArrayList(feed(1), feed(2)));
    }

    private void givenEventApiReturnsOnlyFeed2() {
        when(eventApiService.getUserFeeds())
                .thenReturn(List.of(feed(2)));
    }

    private void givenUpsReturnsNoDefaultFeed() {
        when(userProfileService.getUserDefaultFeed()).thenReturn(null);
    }

    private void givenUpsReturnsPublicFeed2AsDefault() {
        when(userProfileService.getUserDefaultFeed()).thenReturn(publicFeed2.getFeed());
    }

    private void assertDto1(boolean isDefault, EventFeedDto dto) {
        assertEquals(publicFeed1.getFeed(), dto.getFeed());
        assertEquals(publicFeed1.getDescription(), dto.getDescription());
        assertEquals(isDefault, dto.isDefault());
    }

    private void assertDto2(boolean isDefault, EventFeedDto dto) {
        assertEquals(publicFeed2.getFeed(), dto.getFeed());
        assertEquals(publicFeed2.getDescription(), dto.getDescription());
        assertEquals(isDefault, dto.isDefault());
    }

    private EventFeedDto feed(int param) {
        return new EventFeedDto("feed-" + param, "I am a feed" + param);
    }

}
