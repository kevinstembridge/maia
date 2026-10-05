import {describeStrategy, parameterDefinitionsFor, toDescriptors} from './toggles-strategy-editing';
import {ActivationStrategyDefinitionDto} from '../models/ActivationStrategyDefinitionDto';

const definitions: ActivationStrategyDefinitionDto[] = [
    {
        id: 'usernameStrategy',
        description: 'Active only for the listed users.',
        parameters: [{name: 'usernames', description: 'Comma-separated usernames', required: true}],
    },
    {id: 'alwaysActiveStrategy', description: null, parameters: []},
];

describe('parameterDefinitionsFor', () => {

    it('returns the defined parameters of a known strategy', () => {
        expect(parameterDefinitionsFor('usernameStrategy', definitions, [])).toEqual(definitions[0].parameters);
    });

    it('returns no parameters for a known strategy without any', () => {
        expect(parameterDefinitionsFor('alwaysActiveStrategy', definitions, [])).toEqual([]);
    });

    it('falls back to the saved parameters, as optional, for an unknown strategy', () => {
        expect(parameterDefinitionsFor('retiredStrategy', definitions, [{name: 'foo', value: 'bar'}]))
            .toEqual([{name: 'foo', description: null, required: false}]);
    });

});

describe('toDescriptors', () => {

    it('converts rows to descriptors', () => {
        expect(toDescriptors([{id: 'usernameStrategy', parameters: {usernames: 'alice,bob'}}]))
            .toEqual([{id: 'usernameStrategy', parameters: [{name: 'usernames', value: 'alice,bob'}]}]);
    });

    it('trims values and drops blank parameters', () => {
        expect(toDescriptors([{id: 's', parameters: {a: '  x  ', b: '   ', c: ''}}]))
            .toEqual([{id: 's', parameters: [{name: 'a', value: 'x'}]}]);
    });

    it('keeps strategies that have no parameters', () => {
        expect(toDescriptors([{id: 'alwaysActiveStrategy', parameters: {}}]))
            .toEqual([{id: 'alwaysActiveStrategy', parameters: []}]);
    });

    it('returns an empty list for no rows', () => {
        expect(toDescriptors([])).toEqual([]);
    });

});

describe('describeStrategy', () => {

    it('shows just the id when there are no parameters', () => {
        expect(describeStrategy({id: 'alwaysActiveStrategy', parameters: []})).toBe('alwaysActiveStrategy');
    });

    it('shows parameters after the id', () => {
        expect(describeStrategy({id: 'usernameStrategy', parameters: [{name: 'usernames', value: 'alice'}, {name: 'x', value: 'y'}]}))
            .toBe('usernameStrategy (usernames=alice, x=y)');
    });

});
