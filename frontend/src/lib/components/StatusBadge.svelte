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
					label: 'OPEN',
					bg: '#1446A0',
					text: '#EFF4FF',
					border: '#1446A0'
				};
			case 'IN_PROGRESS':
				return {
					label: 'IN PROGRESS',
					bg: '#C49420',
					text: '#FFF8E6',
					border: '#C49420'
				};
			case 'COMPLETED':
				return {
					label: 'COMPLETED',
					bg: '#1A8A55',
					text: '#EDFBF3',
					border: '#1A8A55'
				};
			case 'VERIFIED':
				return {
					label: 'VERIFIED',
					bg: '#7C3FE4',
					text: '#F5EEFF',
					border: '#7C3FE4'
				};
			case 'CANCELLED':
				return {
					label: 'CANCELLED',
					bg: '#6B7280',
					text: '#F3F4F6',
					border: '#6B7280'
				};
			default:
				return {
					label: normalized || 'UNKNOWN',
					bg: '#6A84AC',
					text: '#FFFFFF',
					border: '#6A84AC'
				};
		}
	});

	const sizeClasses = $derived.by(() => {
		switch (size) {
			case 'sm':
				return 'text-[10px] px-2 py-0.5 tracking-wider';
			case 'lg':
				return 'text-xs px-3.5 py-1 tracking-widest font-bold';
			default:
				return 'text-[11px] px-2.5 py-0.5 tracking-wider font-semibold';
		}
	});
</script>

<span
	class="inline-flex items-center uppercase font-mono border {sizeClasses} select-none"
	style="background-color: {badgeConfig.bg}; color: {badgeConfig.text}; border-color: {badgeConfig.border}; border-radius: 0;"
>
	{badgeConfig.label}
</span>
