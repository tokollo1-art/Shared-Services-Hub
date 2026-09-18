<script>
	import StatusBadge from '$lib/components/StatusBadge.svelte';
	import { formatCurrency, formatDuration, formatDate } from '$lib/utils/formatters.js';

	/**
	 * @typedef {Object} Task
	 * @property {number} id
	 * @property {string} title
	 * @property {string} [description]
	 * @property {string} category
	 * @property {string} status
	 * @property {number} [budget]
	 * @property {number} [durationDays]
	 * @property {boolean} [isPaid]
	 * @property {number} [smeId]
	 * @property {string} [smeBusinessName]
	 * @property {number} [assignedToId]
	 * @property {string} [assignedToName]
	 * @property {string} [createdAt]
	 */

	/**
	 * @typedef {Object} Props
	 * @property {Task} task
	 * @property {boolean} [showActions]
	 * @property {import('svelte').Snippet} [actions]
	 */

	/** @type {Props} */
	let { task, showActions = false, actions } = $props();
</script>

<div
	class="group relative flex flex-col justify-between rounded-2xl border border-slate-200/80 bg-white p-6 shadow-xs hover:border-indigo-200 hover:shadow-xl hover:shadow-indigo-500/5 transition-all duration-200"
>
	<div>
		<!-- Top Metadata Row -->
		<div class="flex items-center justify-between gap-3 mb-3">
			<span
				class="inline-flex items-center rounded-lg bg-indigo-50/80 px-2.5 py-1 text-xs font-semibold text-indigo-700 ring-1 ring-inset ring-indigo-700/10"
			>
				{task.category || 'General'}
			</span>
			<StatusBadge status={task.status} size="sm" />
		</div>

		<!-- Title -->
		<h3 class="text-lg font-bold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-2">
			<a href={`/tasks/${task.id}`} class="focus:outline-none focus:underline">
				<span class="absolute inset-0" aria-hidden="true"></span>
				{task.title}
			</a>
		</h3>

		<!-- Description -->
		{#if task.description}
			<p class="mt-2 text-sm text-slate-600 line-clamp-2 leading-relaxed">
				{task.description}
			</p>
		{/if}

		<!-- SME Business Name -->
		<div class="mt-4 flex items-center gap-2 text-xs font-medium text-slate-500">
			<svg class="h-4 w-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
			</svg>
			<span class="truncate">{task.smeBusinessName || 'SME Partner'}</span>
		</div>
	</div>

	<!-- Bottom Details Row -->
	<div class="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between text-sm">
		<!-- Budget & Wage Guaranteed -->
		<div>
			<div class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Corporate Funded</div>
			<div class="text-base font-extrabold text-indigo-600">
				{formatCurrency(task.budget)}
			</div>
		</div>

		<!-- Duration -->
		<div class="text-right">
			<div class="text-xs text-slate-400 font-medium">Duration</div>
			<div class="text-sm font-semibold text-slate-700 flex items-center gap-1 justify-end">
				<svg class="h-3.5 w-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
				</svg>
				{formatDuration(task.durationDays)}
			</div>
		</div>
	</div>

	{#if showActions && actions}
		<div class="relative z-10 mt-4 pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
			{@render actions()}
		</div>
	{/if}
</div>
