<script>
	import StatusBadge from '$lib/components/StatusBadge.svelte';
	import { formatCurrency, formatDuration } from '$lib/utils/formatters.js';

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
	class="group relative flex flex-col justify-between bg-white border border-[#C8D4E8] p-6 shadow-xs hover:border-[#1446A0] hover:shadow-md transition-all"
>
	<div>
		<!-- Top Metadata Row -->
		<div class="flex items-center justify-between gap-3 mb-3">
			<span
				class="inline-block bg-[#EFF4FF] border border-[#C8D4E8] px-2 py-0.5 text-[11px] font-mono font-semibold text-[#1446A0] uppercase tracking-wider"
			>
				{task.category || 'General'}
			</span>
			<StatusBadge status={task.status} size="sm" />
		</div>

		<!-- Title (Serif Editorial) -->
		<h3 class="font-serif text-xl font-semibold text-[#0D1B3E] group-hover:text-[#1446A0] transition-colors line-clamp-2 leading-snug">
			<a href={`/tasks/${task.id}`} class="focus:outline-none focus:underline">
				<span class="absolute inset-0" aria-hidden="true"></span>
				{task.title}
			</a>
		</h3>

		<!-- Description -->
		{#if task.description}
			<p class="mt-2.5 text-sm text-[#465A7A] line-clamp-2 leading-relaxed">
				{task.description}
			</p>
		{/if}

		<!-- SME Business Name -->
		<div class="mt-4 flex items-center gap-2 text-xs font-medium text-[#6A84AC]">
			<span class="font-mono text-[11px] uppercase tracking-wider text-[#6A84AC]">SME</span>
			<span class="text-[#0D1B3E] font-semibold truncate">{task.smeBusinessName || 'SME Partner'}</span>
		</div>
	</div>

	<!-- Bottom Details Row -->
	<div class="mt-6 pt-4 border-t border-[#C8D4E8] flex items-center justify-between text-sm">
		<!-- Budget & Wage Guaranteed -->
		<div>
			<div class="text-[10px] font-mono font-bold uppercase tracking-wider text-[#6A84AC]">Corporate Reserve</div>
			<div class="font-mono text-base font-bold text-[#1446A0] mt-0.5">
				{formatCurrency(task.budget)}
			</div>
		</div>

		<!-- Duration -->
		<div class="text-right">
			<div class="text-[10px] font-mono font-bold uppercase tracking-wider text-[#6A84AC]">Duration</div>
			<div class="font-mono text-sm font-semibold text-[#0D1B3E] mt-0.5">
				{formatDuration(task.durationDays)}
			</div>
		</div>
	</div>

	{#if showActions && actions}
		<div class="relative z-10 mt-4 pt-3 border-t border-[#C8D4E8] flex items-center justify-end gap-2">
			{@render actions()}
		</div>
	{/if}
</div>
