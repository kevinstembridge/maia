import {Component, inject, signal, ChangeDetectionStrategy} from '@angular/core';
import {applyEach, form, FormField, required, validate} from '@angular/forms/signals';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ActivationStrategyDefinitionDto, ActivationStrategyParameterDefinitionDto} from '../../models/ActivationStrategyDefinitionDto';
import {ActivationStrategyDescriptor} from '@app/gen-components/org/maiaframework/toggles/activation/ActivationStrategyDescriptor';
import {parameterDefinitionsFor, StrategyRowValue, toDescriptors} from '../../state/toggles-strategy-editing';

export interface EditStrategiesDialogData {
    featureName: string;
    strategies: ReadonlyArray<ActivationStrategyDescriptor>;
    definitions: ActivationStrategyDefinitionDto[];
}

export interface EditStrategiesDialogResult {
    strategies: ActivationStrategyDescriptor[];
}

interface StrategyRowModel {
    id: string;
    // Keyed by parameter name. The keys change as the selected strategy changes.
    parameters: Record<string, string>;
    // Parameters saved for the row at open time, used to keep the parameters of strategies that
    // the server no longer knows about.
    savedParameters: ActivationStrategyDescriptor['parameters'];
}

@Component({
    selector: 'maia-edit-strategies-dialog',
    templateUrl: './edit-strategies-dialog.html',
    styleUrl: './edit-strategies-dialog.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormField, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule]
})
export class EditStrategiesDialog {

    readonly data = inject<EditStrategiesDialogData>(MAT_DIALOG_DATA);

    private readonly dialogRef = inject(MatDialogRef<EditStrategiesDialog>);

    private readonly model = signal<{ rows: StrategyRowModel[] }>({
        rows: this.data.strategies.map(strategy => ({
            id: strategy.id,
            parameters: this.parametersFor(strategy.id, strategy.parameters, strategy.parameters),
            savedParameters: strategy.parameters,
        })),
    });

    readonly form = form(this.model, (schemaPath) => {
        applyEach(schemaPath.rows, (row) => {
            required(row.id);
            validate(row, ({value}) => {
                const {id, parameters, savedParameters} = value();
                const missing = parameterDefinitionsFor(id, this.data.definitions, savedParameters)
                    .some(definition => definition.required && (parameters[definition.name] ?? '') === '');
                return missing ? {kind: 'requiredParameter', message: 'A required parameter is missing.'} : null;
            });
        });
    });

    addRow(): void {

        this.model.update(m => ({rows: [...m.rows, {id: '', parameters: {}, savedParameters: []}]}));

    }

    removeRow(index: number): void {

        this.model.update(m => ({rows: m.rows.filter((_, i) => i !== index)}));

    }

    onStrategyChanged(index: number, id: string): void {

        this.model.update(m => ({
            rows: m.rows.map((row, i) =>
                i === index ? {...row, id, parameters: this.parametersFor(id, row.savedParameters, [])} : row
            ),
        }));

    }

    parameterDefinitions(row: StrategyRowModel): ActivationStrategyParameterDefinitionDto[] {

        return parameterDefinitionsFor(row.id, this.data.definitions, row.savedParameters);

    }

    /** The strategy ids to offer for a row, including its current id if the server doesn't know it. */
    optionsFor(row: StrategyRowModel): { id: string; label: string }[] {

        const options = this.data.definitions.map(d => ({id: d.id, label: d.id}));

        if (row.id !== '' && !options.some(o => o.id === row.id)) {
            options.push({id: row.id, label: `${row.id} (unknown)`});
        }

        return options;

    }

    descriptionFor(row: StrategyRowModel): string | null {

        return this.data.definitions.find(d => d.id === row.id)?.description ?? null;

    }

    onSubmit(event: Event): void {

        event.preventDefault();

        if (this.form().invalid()) {
            this.form().markAsTouched();
            return;
        }

        const rowValues: StrategyRowValue[] = this.model().rows.map(row => ({
            id: row.id,
            parameters: row.parameters,
        }));

        const result: EditStrategiesDialogResult = {strategies: toDescriptors(rowValues)};
        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

    private parametersFor(
        id: string,
        savedParameters: ActivationStrategyDescriptor['parameters'],
        values: ActivationStrategyDescriptor['parameters']
    ): Record<string, string> {

        const parameters: Record<string, string> = {};

        parameterDefinitionsFor(id, this.data.definitions, savedParameters).forEach(definition => {
            parameters[definition.name] = values.find(p => p.name === definition.name)?.value ?? '';
        });

        return parameters;

    }

}
