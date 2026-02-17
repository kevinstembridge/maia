import {DateTime} from 'luxon';
import {isEffective, isNotEffective, wasEffectiveInThePast} from './date-time-functions';
import { describe, expect, it } from 'vitest';


describe('DateUtilFunctions', () => {


    describe('isEffective()', () => {

        [
            {
                description: 'should return false when to and from are undefined',
                effectiveFrom: undefined,
                effectiveTo: undefined,
                expectedResult: false
            },
            {
                description: 'should return false when effectiveFrom is in the future',
                effectiveFrom: oneHourFromNow(),
                effectiveTo: undefined,
                expectedResult: false
            },
            {
                description: 'should return false when effectiveTo is in the past',
                effectiveFrom: twoHoursAgo(),
                effectiveTo: onHourAgo(),
                expectedResult: false
            },
            {
                description: 'should return true when effectiveFrom is in the past and effectiveTo is not set',
                effectiveFrom: onHourAgo(),
                effectiveTo: undefined,
                expectedResult: true
            },
            {
                description: 'should return true when effectiveFrom is in the past and effectiveTo is in the future',
                effectiveFrom: onHourAgo(),
                effectiveTo: oneHourFromNow(),
                expectedResult: true
            },
        ].forEach((fixture: {effectiveFrom: string | undefined, effectiveTo: string | undefined, description: string, expectedResult: boolean}) => {


            it(fixture.description, () => {
                expect(isEffective(fixture)).toEqual(fixture.expectedResult);
                expect(isNotEffective(fixture)).not.toEqual(fixture.expectedResult);
            });


        });


    });


    describe('wasEffectiveInThePast()', () => {

        [
            {
                description: 'should return false when to and from are undefined',
                effectiveFrom: undefined,
                effectiveTo: undefined,
                expectedResult: false
            },
            {
                description: 'should return false when effectiveFrom is in the future',
                effectiveFrom: oneHourFromNow(),
                effectiveTo: undefined,
                expectedResult: false
            },
            {
                description: 'should return true when effectiveFrom and effectiveTo are in the past',
                effectiveFrom: twoHoursAgo(),
                effectiveTo: onHourAgo(),
                expectedResult: true
            },
            {
                description: 'should return false when effectiveFrom is in the past and effectiveTo is not set',
                effectiveFrom: onHourAgo(),
                effectiveTo: undefined,
                expectedResult: false
            },
            {
                description: 'should return false when effectiveFrom is in the past and effectiveTo is in the future',
                effectiveFrom: onHourAgo(),
                effectiveTo: oneHourFromNow(),
                expectedResult: false
            },
        ].forEach((fixture: {effectiveFrom?: string, effectiveTo?: string, description: string, expectedResult: boolean}) => {


            it(fixture.description, () => {
                expect(wasEffectiveInThePast(fixture)).toEqual(fixture.expectedResult);
            });


        });


    });

});


function oneHourFromNow() {

    return DateTime.now().plus({hours: 1}).toISO() as string;

}


function twoHoursAgo() {

    return DateTime.now().minus({hours: 2}).toISO() as string;

}


function onHourAgo() {

    return DateTime.now().minus({hours: 1}).toISO() as string;

}
