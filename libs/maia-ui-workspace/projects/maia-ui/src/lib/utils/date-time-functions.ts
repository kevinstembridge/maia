import {DateTime} from 'luxon';


function effectiveFromIsInThePast(range: { effectiveFrom?: string; effectiveTo?: string }, now: number): boolean {

    return !!range.effectiveFrom && DateTime.fromISO(range.effectiveFrom).toMillis() < now;

}


function effectiveToIsInThePast(range: { effectiveFrom?: string; effectiveTo?: string }, now: number): boolean {

    return !!range.effectiveTo && DateTime.fromISO(range.effectiveTo).toMillis() < now;

}


function effectiveToIsNotSet(range: { effectiveFrom?: string; effectiveTo?: string }): boolean {

    return !(range.effectiveTo);

}


function effectiveToIsInTheFuture(range: { effectiveFrom?: string; effectiveTo?: string }, now: number): boolean {

    return !!range.effectiveTo && DateTime.fromISO(range.effectiveTo).toMillis() > now;

}


export function isEffective(
    range: { effectiveFrom?: string; effectiveTo?: string }
): boolean {

    const now = DateTime.now().toMillis();
    return effectiveFromIsInThePast(range, now) && (effectiveToIsNotSet(range) || effectiveToIsInTheFuture(range, now));

}


export function isNotEffective(
    range: { effectiveFrom?: string; effectiveTo?: string }
): boolean {

    return isEffective(range) === false;

}


export function wasEffectiveInThePast(
    range: { effectiveFrom?: string, effectiveTo?: string }
): boolean {

    const now = DateTime.now().toMillis();
    return effectiveFromIsInThePast(range, now) && effectiveToIsInThePast(range, now);

}
