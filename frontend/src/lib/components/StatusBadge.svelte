<script>
	/**
	 * @typedef {Object} Props
	 * @property {string} [status]
	 * @property {'sm'|'md'|'lg'} [size]
	 */

	/** @type {Props} */
	let { status = 'OPEN', size = 'md' } = $props();

	const normalized = $derived((status || '').toUpperCase().trim());

	const badgeConfig = $derived.by(() => {
		switch (normalized) {
			case 'OPEN':
				return {
					label: 'Open',
					classes: 'bg-blue-50 text-blue-700 border-blue-200 ring-blue-500/10',
					dot: 'bg-blue-500 animate-pulse'
				};
			case 'IN_PROGRESS':
				return {
					label: 'In Progress',
					classes: 'bg-amber-50 text-amber-800 border-amber-200 ring-amber-500/10',
					dot: 'bg-amber-500'
				};
			case 'COMPLETED':
				return {
					label: 'Completed',
					classes: 'bg-emerald-50 text-emerald-700 border-emerald-200 ring-emerald-500/10',
					dot: 'bg-emerald-500'
				};
			case 'VERIFIED':
				return {
					label: 'Verified',
					classes: 'bg-purple-50 text-purple-700 border-purple-200 ring-purple-500/10',
					dot: 'bg-purple-600'
				};
			case 'CANCELLED':
				return {
					label: 'Cancelled',
					classes: 'bg-rose-50 text-rose-700 border-rose-200 ring-rose-500/10',
					dot: 'bg-rose-500'
				};
			default:
				return {
					label: normalized || 'Unknown',
					classes: 'bg-slate-100 text-slate-700 border-slate-200 ring-slate-500/10',
					dot: 'bg-slate-400'
				};
		}
	});

	const sizeClasses = $derived.by(() => {
		switch (size) {
			case 'sm':
				return 'text-xs px-2 py-0.5 gap-1.5';
			case 'lg':
				return 'text-sm px-3.5 py-1.5 gap-2 font-semibold';
			default:
				return 'text-xs px-2.5 py-1 gap-1.5 font-medium';
		}
	});
</script>

<span
	class="inline-flex items-center rounded-full border shadow-xs transition-colors {badgeConfig.classes} {sizeClasses}"
>
	<span class="h-1.5 w-1.5 rounded-full shrink-0 {badgeConfig.dot}"></span>
	<span>{badgeConfig.label}</span>
</span>
